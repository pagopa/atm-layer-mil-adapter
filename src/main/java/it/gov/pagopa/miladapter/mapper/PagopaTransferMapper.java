package it.gov.pagopa.miladapter.mapper;

import it.gov.pagopa.miladapter.services.dto.PagopaTransferListDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransferListRequest;
import it.gov.pagopa.miladapter.services.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PagopaTransferMapper {

    @Mapping(target = "transactionId", source = "request.transactionId")
    @Mapping(target = "transferId", source = "transfer.idTransfer")
    @Mapping(target = "transferAmount", source = "transfer.transferAmount")
    @Mapping(target = "pagopaReported", source = "request.pagopaReported")
    @Mapping(target = "transferExecutionDt", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "paFiscalCode", source = "transfer.paTaxCode")
    @Mapping(target = "paName", source = "transfer.company")
    @Mapping(target = "paIban", source = "transfer.iban")
    @Mapping(target = "rmtInfo", source = "transfer.remittanceInformation")
    PagopaTransferListDto toDto(PagoPaTransferListRequest request, Transfer transfer);

    default List<PagopaTransferListDto> toDtoList(PagoPaTransferListRequest request) {
        if (request == null || request.getTransfers() == null) {
            return Collections.emptyList();
        }
        return request.getTransfers().stream()
                .map(transfer -> toDto(request, transfer))
                .toList();
    }
}
