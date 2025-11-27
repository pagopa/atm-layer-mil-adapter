package it.gov.pagopa.miladapter.redis;

import it.gov.pagopa.miladapter.services.model.Notice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Redis client, reactive flavor.
 * Used to store/retrieve the payment notices data returned by the node in the activatePayment API
 */
@Service
public class PaymentNoticeService {

    private final ReactiveValueOperations<String, Notice> operations;

    public PaymentNoticeService(ReactiveRedisTemplate<String, Notice> redisTemplate) {
        this.operations = redisTemplate.opsForValue();
    }

    /**
     * Returns a list of cached payment notices.
     *
     * @param paymentTokens a list of payment tokens
     * @return a {@link Mono} containing the payment notices as values or null if not found
     */
    public Mono<Map<String, Notice>> mget(List<String> paymentTokens) {
        return operations.multiGet(paymentTokens)
                .map(values -> {
                    Map<String, Notice> result = new HashMap<>();
                    for (int i = 0; i < paymentTokens.size(); i++) {
                        if (i < values.size() && values.get(i) != null) {
                            result.put(paymentTokens.get(i), values.get(i));
                        }
                    }
                    return result;
                });
    }

    /**
     * Stores a payment notice.
     *
     * @param paymentToken the ID of the payment
     * @return a {@link Mono} emitting Boolean (success/failure)
     */
    public Mono<Boolean> set(String paymentToken, Notice notice) {
        return operations.set(paymentToken, notice);
    }
}
