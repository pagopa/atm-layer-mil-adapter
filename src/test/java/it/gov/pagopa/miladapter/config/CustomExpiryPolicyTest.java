package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.model.Token;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CustomExpiryPolicyTest {

    @Test
    public void testGetExpiryForCreation() {
        CustomExpiryPolicy customExpiryPolicy = new CustomExpiryPolicy();
        Token token = Mockito.mock(Token.class);
        when(token.getExpires_in()).thenReturn(900);
        Duration duration = customExpiryPolicy.getExpiryForCreation("String", token);
        assertEquals(Duration.ofSeconds(900), duration);
    }

    @Test
    public void testGetExpiryForAccess() {
        CustomExpiryPolicy customExpiryPolicy = new CustomExpiryPolicy();
        Supplier<? extends Token> supplier = (Supplier<? extends Token>) mock(Supplier.class);
        Duration duration = customExpiryPolicy.getExpiryForAccess("String", supplier);
        assertNull(duration);
    }

    @Test
    void testGetExpiryForUpdate() {
        CustomExpiryPolicy customExpiryPolicy = new CustomExpiryPolicy();
        Supplier<? extends Token> supplier = (Supplier<? extends Token>) mock(Supplier.class);
        Token token = Mockito.mock(Token.class);
        when(token.getExpires_in()).thenReturn(30);
        Duration duration = customExpiryPolicy.getExpiryForUpdate("String", supplier, token);
        assertEquals(Duration.ofSeconds(30), duration);
    }
}
