package it.gov.pagopa.miladapter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class KeyTokenTest {
    @Test
    public void testEqualsAndHashCode(){
        KeyToken keyToken1 = new KeyToken("64874412","06789","ATM");
        KeyToken keyToken2 = new KeyToken("64874412","06789","ATM");
        assertEquals(keyToken1, keyToken2);
        assertEquals(keyToken1.hashCode(), keyToken2.hashCode());
    }

    @Test
    public void testNoArgsConstructor(){
        KeyToken keyToken = new KeyToken();
        assertNotNull(keyToken);
        assertNull(keyToken.getChannel());
        assertNull(keyToken.getTerminalId());
        assertNull(keyToken.getAcquirerId());
    }

    @Test
    public void testAllArgsConstructor(){
        KeyToken keyToken = new KeyToken("64874412","06789","ATM");
        assertEquals("64874412", keyToken.getTerminalId());
        assertEquals("06789", keyToken.getAcquirerId());
        assertEquals("ATM", keyToken.getChannel());
    }

    @Test
    public void testSetter(){
        KeyToken keyToken = new KeyToken();
        keyToken.setChannel("ATM");
        keyToken.setTerminalId("64874412");
        keyToken.setAcquirerId("06789");
    }

    @Test
    public void testConstructor() {
        assertEquals("ATM_06789_64874412", (new KeyToken("64874412", "06789", "ATM")).toString());
    }
}
