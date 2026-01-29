package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import it.gov.pagopa.miladapter.enums.FlowValues;
import it.gov.pagopa.miladapter.enums.MilValues;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import it.gov.pagopa.miladapter.services.model.*;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.sonarsource.scanner.api.internal.shaded.minimaljson.JsonObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static it.gov.pagopa.miladapter.util.LogSanitizer.sanitizeForLog;

@Slf4j
@Service
public class ExternalCallServiceImpl extends GenericRestExternalServiceAbstract
    implements ExternalCallService {

  private final RestConfigurationProperties restConfigurationProperties;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final VerifyPaymentNoticeService verifyPaymentNoticeService;
  private final ActivatePaymentNoticeService activatePaymentNoticeService;
  private final PaymentService paymentService;
  private final ReportingService reportingService;

  private static final Pattern QR_CODE_PATTERN =
      Pattern.compile("/mil-payment-notice/paymentNotices/([^/]+)$");
  private static final Pattern TAX_CODE_NOTICE_PATTERN =
      Pattern.compile("/mil-payment-notice/paymentNotices/([^/]+)/([^/]+)$");
  private static final Pattern CLOSE_PATTERN =
      Pattern.compile("/mil-payment-notice/payments/([^/]+)/sendPaymentOutcome");
  private static final Pattern TRANSACTIONS_PATTERN = Pattern.compile("/transactions");
  private static final Pattern TRANSFER_LISTS_PATTERN = Pattern.compile("/transfer-lists");
  private final List<Route> routes;

  interface Handler {
    ResponseEntity<?> handle(CommonHeader h, Map<String, String> pathParams, String body)
        throws JsonProcessingException;
  }

  record Route(Pattern pattern, HttpMethod method, Handler handler) {}

  public ExternalCallServiceImpl(
      RestConfigurationProperties restConfigurationProperties,
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      VerifyPaymentNoticeService verifyPaymentNoticeService,
      ActivatePaymentNoticeService activatePaymentNoticeService,
      PaymentService paymentService,
      ReportingService reportingService) {
    this.restConfigurationProperties = restConfigurationProperties;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.verifyPaymentNoticeService = verifyPaymentNoticeService;
    this.activatePaymentNoticeService = activatePaymentNoticeService;
    this.paymentService = paymentService;
    this.reportingService = reportingService;

    this.routes =
        List.of(
            new Route(
                QR_CODE_PATTERN,
                HttpMethod.GET,
                (h, pathParams, body) ->
                    this.verifyPaymentNoticeService.verifyByQrCode(
                        h, pathParams.get(MilValues.QRCODE.getValue()))),
            new Route(
                QR_CODE_PATTERN,
                HttpMethod.PATCH,
                (h, pathParams, body) -> {
                  ActivatePaymentNoticeRequest request =
                      this.objectMapper.readValue(body, ActivatePaymentNoticeRequest.class);
                  return this.activatePaymentNoticeService.activateByQrCode(
                      h, pathParams.get(MilValues.QRCODE.getValue()), request);
                }),
            new Route(
                TAX_CODE_NOTICE_PATTERN,
                HttpMethod.GET,
                (h, pathParams, body) ->
                    this.verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
                        h,
                        pathParams.get(MilValues.PA_TAX_CODE.getValue()),
                        pathParams.get(MilValues.NOTICE_NUMBER.getValue()))),
            new Route(
                TAX_CODE_NOTICE_PATTERN,
                HttpMethod.PATCH,
                (h, pathParams, body) -> {
                  ActivatePaymentNoticeRequest request =
                      this.objectMapper.readValue(body, ActivatePaymentNoticeRequest.class);
                  return this.activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
                      h,
                      pathParams.get(MilValues.PA_TAX_CODE.getValue()),
                      pathParams.get(MilValues.NOTICE_NUMBER.getValue()),
                      request);
                }),
            new Route(
                CLOSE_PATTERN,
                HttpMethod.PATCH,
                (h, pathParams, body) -> {
                  ClosePaymentRequest request =
                      this.objectMapper.readValue(body, ClosePaymentRequest.class);
                  return this.paymentService.sendPaymentOutcome(
                      h, pathParams.get(MilValues.TRANSACTION_ID.getValue()), request);
                }),
            new Route(
                TRANSACTIONS_PATTERN,
                HttpMethod.POST,
                (h, pathParams, body) -> {
                    PagoPaTransactionRequest request =
                        this.objectMapper.readValue(body, PagoPaTransactionRequest.class);
                    return this.reportingService.createTransaction(request);
                }),
            new Route(
                TRANSFER_LISTS_PATTERN,
                HttpMethod.POST,
                (h, pathParams, body) -> {
                    PagoPaTransferListRequest request =
                        this.objectMapper.readValue(body, PagoPaTransferListRequest.class);
                    return this.reportingService.createTransferList(request);
                })
            );
  }

  @Override
  public URI prepareUri(Configuration configuration, String flow) {
    if (flow.equals(FlowValues.MIL.getValue())) {
      return HttpRequestUtils.buildURI(
          this.restConfigurationProperties.getMilBasePath(),
          configuration.getEndpoint(),
          configuration.getPathParams());
    } else if (flow.equals(FlowValues.IDPAY.getValue())) {
      return HttpRequestUtils.buildURI(
          this.restConfigurationProperties.getIdPayBasePath(),
          configuration.getEndpoint(),
          configuration.getPathParams());
    } else if (flow.equals(FlowValues.AUTH.getValue())) {
      log.info("--TEMPORARY-- Preparing URI for flow {}", flow);
      log.info(
          "--TEMPORARY-- Mil Base path: {} , auth endpoint: {}",
          this.restConfigurationProperties.getMilBasePath(),
          this.restConfigurationProperties.getGetTokenEndpoint());
      return HttpRequestUtils.buildURI(
          this.restConfigurationProperties.getMilBasePath(),
          this.restConfigurationProperties.getGetTokenEndpoint());
    } else {
      throw new RuntimeException("Unrecognised flow: " + flow);
    }
  }

  @Override
  public SpanBuilder spanBuilder(Configuration configuration) {
    return super.spanBuilder(configuration);
  }

  public ResponseEntity executeExternalCall(Map<String, Object> body)
      throws JsonProcessingException {
    String flow = body.get(RequiredProcessVariables.FLOW.getEngineValue()).toString();
    Configuration configuration;

    if (flow.equals(FlowValues.AUTH.getValue())) {
      configuration =
          EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationGenerateTokenCall(
              body, this.restConfigurationProperties.getAuth());
    } else {
      configuration =
          EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCall(
              body,
              flow.equals(FlowValues.MIL.getValue()),
              flow.equals(FlowValues.IDPAY.getValue()));
    }

    // Handle MIL flow
    if (flow.equals(FlowValues.MIL.getValue()) && isLocalMilEndpoint(configuration.getEndpoint())) {
      return handleLocalMilCall(configuration);
    }

    return executeHttpCall(configuration, flow);
  }

  private static boolean isLocalMilEndpoint(String endpoint) {
    return QR_CODE_PATTERN.matcher(endpoint).matches()
        || TAX_CODE_NOTICE_PATTERN.matcher(endpoint).matches()
        || CLOSE_PATTERN.matcher(endpoint).matches()
        || TRANSACTIONS_PATTERN.matcher(endpoint).matches()
        || TRANSFER_LISTS_PATTERN.matcher(endpoint).matches();
  }

  protected ResponseEntity handleLocalMilCall(Configuration configuration)
      throws JsonProcessingException {
    SpanBuilder spanBuilder = this.spanBuilder(configuration);
    Span serviceSpan = spanBuilder.startSpan();

    try (Scope scope = serviceSpan.makeCurrent()) {
      serviceSpan.setAttribute("call.type", "local");
      serviceSpan.setAttribute("MIL.call.start.time", LocalDateTime.now().toString());

      // Extract CommonHeader from configuration headers
      CommonHeader headers = extractCommonHeader(configuration);

      ResponseEntity<?> controllerResponse =
          routeToService(
              configuration.getEndpoint(),
              configuration.getPathParams(),
              configuration.getHttpMethod(),
              configuration.getBody(),
              headers);

      serviceSpan.setAttribute("MIL.call.end.time", LocalDateTime.now().toString());
      serviceSpan.setAttribute(
          SemanticAttributes.HTTP_STATUS_CODE, controllerResponse.getStatusCode().value());

      String responseBody = this.objectMapper.writeValueAsString(controllerResponse.getBody());

      return new ResponseEntity<>(responseBody, controllerResponse.getStatusCode());

    } catch (Exception e) {
      log.error("Exception in local MIL call", e);
      serviceSpan.setAttribute(
          SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
      String errorJson = objectMapper.writeValueAsString(Map.of("error", e.getLocalizedMessage()));
      return new ResponseEntity<>(errorJson, HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      serviceSpan.end();
    }
  }

  private CommonHeader extractCommonHeader(Configuration configuration) {
    HttpHeaders headers = configuration.getHeaders();

    CommonHeader commonHeader = new CommonHeader();
    commonHeader.setAcquirerId(
        String.valueOf(
            Objects.requireNonNull(
                    headers.get(RequiredProcessVariables.ACQUIRER_ID.getAuthenticatorValue()))
                .getFirst()));
    commonHeader.setChannel(
        String.valueOf(
            Objects.requireNonNull(
                    headers.get(RequiredProcessVariables.CHANNEL.getAuthenticatorValue()))
                .getFirst()));
    commonHeader.setTerminalId(
        String.valueOf(
            Objects.requireNonNull(
                    headers.get(RequiredProcessVariables.TERMINAL_ID.getAuthenticatorValue()))
                .getFirst()));
    commonHeader.setRequestId(
        String.valueOf(
            Objects.requireNonNull(
                    headers.get(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()))
                .getFirst()));

    return commonHeader;
  }

  private ResponseEntity<?> routeToService(
      String endpoint,
      Map<String, String> pathParams,
      HttpMethod httpMethod,
      String body,
      CommonHeader headers)
      throws JsonProcessingException {
    for (Route r : routes) {
      Matcher matcher = r.pattern.matcher(endpoint);
      if (matcher.matches() && r.method.equals(httpMethod)) {
        return r.handler.handle(headers, pathParams, body);
      }
    }

    String message =
        String.format(
            "Unsupported local MIL operation for endpoint: %s with method: %s",
            endpoint, httpMethod);
    log.error(sanitizeForLog(message));
    return new ResponseEntity<>(new JsonObject().toString(), HttpStatus.NOT_IMPLEMENTED);
  }

  private ResponseEntity executeHttpCall(Configuration configuration, String flow) {
    ResponseEntity<String> response;

    SpanBuilder spanBuilder = this.spanBuilder(configuration);
    Span serviceSpan = spanBuilder.startSpan();

    try (Scope scope = serviceSpan.makeCurrent()) {
      URI url = this.prepareUri(configuration, flow);
      HttpEntity<String> entity =
          HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
      serviceSpan.setAttribute(
          SemanticAttributes.HTTP_METHOD, configuration.getHttpMethod().name());
      serviceSpan.setAttribute(SemanticAttributes.HTTP_URL, url.toString());

      if (entity.hasBody()) {
        serviceSpan.setAttribute("http.body", entity.getBody());
      }

      serviceSpan.setAttribute("http.headers", entity.getHeaders().toString());
      serviceSpan.setAttribute("MIL.call.start.time", LocalDateTime.now().toString());

      response =
          this.restTemplate.exchange(url, configuration.getHttpMethod(), entity, String.class);
      serviceSpan.setAttribute("MIL.call.end.time", LocalDateTime.now().toString());

      if (response.getBody() == null) {
        response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
      }
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error("Exception in HTTP request", e);
      response = new ResponseEntity<>(new JsonObject().toString(), e.getStatusCode());
      serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
      serviceSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
    } catch (ResourceAccessException e) {
      log.error("Exception in HTTP request", e);
      response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.GATEWAY_TIMEOUT);
    } catch (Exception e) {
      log.error("Exception in HTTP request", e);
      response =
          new ResponseEntity<>(new JsonObject().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.getStatusCode().value());
    serviceSpan.setAttribute("http.response.body", response.getBody());
    serviceSpan.setAttribute("http.response.headers", response.getHeaders().toString());
    serviceSpan.end();

    return response;
  }

  public HttpEntity<String> buildHttpEntity(Configuration configuration) {
    return HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
  }
}
