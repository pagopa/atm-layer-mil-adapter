package it.gov.pagopa.miladapter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class KeyTokenTest {
    @Test
    void testEqualsAndHashCode(){
        KeyToken keyToken1 = new KeyToken("64874412","06789","ATM", "123");
        KeyToken keyToken2 = new KeyToken("64874412","06789","ATM", "123");
        assertEquals(keyToken1, keyToken2);
        assertEquals(keyToken1.hashCode(), keyToken2.hashCode());
    }

    @Test
    void testNoArgsConstructor(){
        KeyToken keyToken = new KeyToken();
        assertNotNull(keyToken);
        assertNull(keyToken.getChannel());
        assertNull(keyToken.getTerminalId());
        assertNull(keyToken.getAcquirerId());
    }

    @Test
    void testAllArgsConstructor(){
        KeyToken keyToken = new KeyToken("64874412","06789","ATM", "123");
        assertEquals("64874412", keyToken.getTerminalId());
        assertEquals("06789", keyToken.getAcquirerId());
        assertEquals("ATM", keyToken.getChannel());
    }

    @Test
    void testSetter(){
        KeyToken keyToken = new KeyToken();
        keyToken.setChannel("ATM");
        keyToken.setTerminalId("64874412");
        keyToken.setAcquirerId("06789");
        assertEquals("ATM", keyToken.getChannel());
        assertEquals("64874412", keyToken.getTerminalId());
        assertEquals("06789", keyToken.getAcquirerId());
    }

    @Test
    void testConstructor() {
        assertEquals("ATM_06789_64874412_123", (new KeyToken("64874412", "ATM", "06789", "123")).toString());
    }
}
