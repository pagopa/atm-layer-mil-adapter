package it.gov.pagopa.miladapter.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QrCodeTest {

  @Test
  void testToString_WithAllFields() {
    // Arrange
    QrCode qrCode = new QrCode();
    qrCode.setIdCode("PAGOPA");
    qrCode.setVersion("002");
    qrCode.setNoticeNumber("123456789012345678");
    qrCode.setPaTaxCode("12345678901");
    qrCode.setAmount("12345");

    // Act
    String result = qrCode.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("idCode='PAGOPA'"));
    assertTrue(result.contains("version='002'"));
    assertTrue(result.contains("noticeNumber='123456789012345678'"));
    assertTrue(result.contains("paTaxCode='12345678901'"));
    assertTrue(result.contains("amount='12345'"));
  }

  @Test
  void testToString_WithNullFields() {
    // Arrange
    QrCode qrCode = new QrCode();

    // Act
    String result = qrCode.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("idCode='null'"));
    assertTrue(result.contains("version='null'"));
    assertTrue(result.contains("noticeNumber='null'"));
    assertTrue(result.contains("paTaxCode='null'"));
    assertTrue(result.contains("amount='null'"));
  }

  @Test
  void testToString_Format() {
    // Arrange
    QrCode qrCode = new QrCode();
    qrCode.setIdCode("PAGOPA");
    qrCode.setVersion("002");
    qrCode.setNoticeNumber("123456789012345678");
    qrCode.setPaTaxCode("12345678901");
    qrCode.setAmount("12345");

    // Act
    String result = qrCode.toString();

    // Assert
    assertTrue(result.startsWith("QrCode{"));
    assertTrue(result.endsWith("}"));
    assertTrue(result.contains("idCode="));
    assertTrue(result.contains("version="));
    assertTrue(result.contains("noticeNumber="));
    assertTrue(result.contains("paTaxCode="));
    assertTrue(result.contains("amount="));
  }

  @Test
  void testGettersAndSetters() {
    // Arrange
    QrCode qrCode = new QrCode();
    String idCode = "PAGOPA";
    String version = "002";
    String noticeNumber = "123456789012345678";
    String paTaxCode = "12345678901";
    String amount = "12345";

    // Act
    qrCode.setIdCode(idCode);
    qrCode.setVersion(version);
    qrCode.setNoticeNumber(noticeNumber);
    qrCode.setPaTaxCode(paTaxCode);
    qrCode.setAmount(amount);

    // Assert
    assertEquals(idCode, qrCode.getIdCode());
    assertEquals(version, qrCode.getVersion());
    assertEquals(noticeNumber, qrCode.getNoticeNumber());
    assertEquals(paTaxCode, qrCode.getPaTaxCode());
    assertEquals(amount, qrCode.getAmount());
  }

  @Test
  void testToString_WithPartialFields() {
    // Arrange
    QrCode qrCode = new QrCode();
    qrCode.setIdCode("PAGOPA");
    qrCode.setVersion("002");
    qrCode.setNoticeNumber(null);
    qrCode.setPaTaxCode("12345678901");
    qrCode.setAmount(null);

    // Act
    String result = qrCode.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("idCode='PAGOPA'"));
    assertTrue(result.contains("version='002'"));
    assertTrue(result.contains("noticeNumber='null'"));
    assertTrue(result.contains("paTaxCode='12345678901'"));
    assertTrue(result.contains("amount='null'"));
  }

  @Test
  void testToString_WithValidAmountFormats() {
    // Arrange - test minimum amount (2 digits)
    QrCode qrCode1 = new QrCode();
    qrCode1.setAmount("10");

    // Arrange - test maximum amount (11 digits)
    QrCode qrCode2 = new QrCode();
    qrCode2.setAmount("12345678901");

    // Act
    String result1 = qrCode1.toString();
    String result2 = qrCode2.toString();

    // Assert
    assertTrue(result1.contains("amount='10'"));
    assertTrue(result2.contains("amount='12345678901'"));
  }

  @Test
  void testToString_WithValidNoticeNumber() {
    // Arrange
    QrCode qrCode = new QrCode();
    qrCode.setNoticeNumber("302000100000009424");

    // Act
    String result = qrCode.toString();

    // Assert
    assertTrue(result.contains("noticeNumber='302000100000009424'"));
  }

  @Test
  void testToString_WithValidPaTaxCode() {
    // Arrange
    QrCode qrCode = new QrCode();
    qrCode.setPaTaxCode("77777777777");

    // Act
    String result = qrCode.toString();

    // Assert
    assertTrue(result.contains("paTaxCode='77777777777'"));
  }
}

