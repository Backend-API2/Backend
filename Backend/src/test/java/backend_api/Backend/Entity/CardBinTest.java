package backend_api.Backend.Entity;

import backend_api.Backend.Entity.payment.types.CardBin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardBinTest {

    private CardBin cardBin;

    @BeforeEach
    void setUp() {
        cardBin = new CardBin();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(cardBin);
        assertNull(cardBin.getId());
        assertNull(cardBin.getBin());
        assertNull(cardBin.getBankName());
        assertNull(cardBin.getCardType());
        assertTrue(cardBin.getIsActive()); // Default value is true
    }

    @Test
    void testParameterizedConstructor() {
        // Test constructor with parameters
        CardBin newCardBin = new CardBin("123", "Test Bank", "Visa");

        assertNotNull(newCardBin);
        assertNull(newCardBin.getId()); // ID is auto-generated
        assertEquals("123", newCardBin.getBin());
        assertEquals("Test Bank", newCardBin.getBankName());
        assertEquals("Visa", newCardBin.getCardType());
        assertTrue(newCardBin.getIsActive()); // Default value is true
    }

    @Test
    void testParameterizedConstructor_WithDifferentValues() {
        // Test constructor with different values
        CardBin newCardBin = new CardBin("456", "Another Bank", "Mastercard");

        assertNotNull(newCardBin);
        assertEquals("456", newCardBin.getBin());
        assertEquals("Another Bank", newCardBin.getBankName());
        assertEquals("Mastercard", newCardBin.getCardType());
        assertTrue(newCardBin.getIsActive());
    }

    @Test
    void testSettersAndGetters() {
        // Test basic setters and getters
        cardBin.setId(1L);
        cardBin.setBin("789");
        cardBin.setBankName("Credit Bank");
        cardBin.setCardType("American Express");
        cardBin.setIsActive(false);

        assertEquals(1L, cardBin.getId());
        assertEquals("789", cardBin.getBin());
        assertEquals("Credit Bank", cardBin.getBankName());
        assertEquals("American Express", cardBin.getCardType());
        assertFalse(cardBin.getIsActive());
    }

    @Test
    void testBinValidation() {
        // Test various BIN formats
        cardBin.setBin("123");
        assertEquals("123", cardBin.getBin());

        cardBin.setBin("456");
        assertEquals("456", cardBin.getBin());

        cardBin.setBin("789");
        assertEquals("789", cardBin.getBin());

        // Test null BIN
        cardBin.setBin(null);
        assertNull(cardBin.getBin());

        // Test empty BIN
        cardBin.setBin("");
        assertEquals("", cardBin.getBin());
    }

    @Test
    void testBankNameValidation() {
        // Test various bank names
        cardBin.setBankName("Chase Bank");
        assertEquals("Chase Bank", cardBin.getBankName());

        cardBin.setBankName("Bank of America");
        assertEquals("Bank of America", cardBin.getBankName());

        cardBin.setBankName("Wells Fargo");
        assertEquals("Wells Fargo", cardBin.getBankName());

        cardBin.setBankName("Citibank");
        assertEquals("Citibank", cardBin.getBankName());

        // Test null bank name
        cardBin.setBankName(null);
        assertNull(cardBin.getBankName());

        // Test empty bank name
        cardBin.setBankName("");
        assertEquals("", cardBin.getBankName());
    }

    @Test
    void testCardTypeValidation() {
        // Test various card types
        cardBin.setCardType("Visa");
        assertEquals("Visa", cardBin.getCardType());

        cardBin.setCardType("Mastercard");
        assertEquals("Mastercard", cardBin.getCardType());

        cardBin.setCardType("American Express");
        assertEquals("American Express", cardBin.getCardType());

        cardBin.setCardType("Discover");
        assertEquals("Discover", cardBin.getCardType());

        cardBin.setCardType("Diners Club");
        assertEquals("Diners Club", cardBin.getCardType());

        cardBin.setCardType("JCB");
        assertEquals("JCB", cardBin.getCardType());

        // Test null card type
        cardBin.setCardType(null);
        assertNull(cardBin.getCardType());

        // Test empty card type
        cardBin.setCardType("");
        assertEquals("", cardBin.getCardType());
    }

    @Test
    void testIsActiveFlag() {
        // Test active flag
        cardBin.setIsActive(true);
        assertTrue(cardBin.getIsActive());

        cardBin.setIsActive(false);
        assertFalse(cardBin.getIsActive());

        // Test null active flag
        cardBin.setIsActive(null);
        assertNull(cardBin.getIsActive());
    }

    @Test
    void testDefaultIsActiveValue() {
        // Test that default constructor sets isActive to true
        CardBin newCardBin = new CardBin();
        assertTrue(newCardBin.getIsActive());

        // Test that parameterized constructor sets isActive to true
        CardBin newCardBin2 = new CardBin("123", "Test Bank", "Visa");
        assertTrue(newCardBin2.getIsActive());
    }

    @Test
    void testToString() {
        cardBin.setId(1L);
        cardBin.setBin("123");
        cardBin.setBankName("Test Bank");
        cardBin.setCardType("Visa");
        cardBin.setIsActive(true);

        String result = cardBin.toString();

        assertNotNull(result);
        assertTrue(result.contains("CardBin"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("bin=123"));
        assertTrue(result.contains("bankName=Test Bank"));
        assertTrue(result.contains("cardType=Visa"));
        assertTrue(result.contains("isActive=true"));
    }

    @Test
    void testEqualsAndHashCode() {
        CardBin cardBin1 = new CardBin();
        cardBin1.setId(1L);
        cardBin1.setBin("123");
        cardBin1.setBankName("Test Bank");
        cardBin1.setCardType("Visa");

        CardBin cardBin2 = new CardBin();
        cardBin2.setId(1L);
        cardBin2.setBin("123");
        cardBin2.setBankName("Test Bank");
        cardBin2.setCardType("Visa");

        CardBin cardBin3 = new CardBin();
        cardBin3.setId(2L);
        cardBin3.setBin("456");
        cardBin3.setBankName("Another Bank");
        cardBin3.setCardType("Mastercard");

        // Test equals
        assertEquals(cardBin1, cardBin2);
        assertNotEquals(cardBin1, cardBin3);
        assertNotEquals(cardBin2, cardBin3);

        // Test hashCode
        assertEquals(cardBin1.hashCode(), cardBin2.hashCode());
        assertNotEquals(cardBin1.hashCode(), cardBin3.hashCode());
    }

    @Test
    void testCompleteCardBin() {
        // Test a complete CardBin with all fields
        cardBin.setId(1L);
        cardBin.setBin("411111");
        cardBin.setBankName("Chase Bank");
        cardBin.setCardType("Visa");
        cardBin.setIsActive(true);

        // Verify all fields are set correctly
        assertEquals(1L, cardBin.getId());
        assertEquals("411111", cardBin.getBin());
        assertEquals("Chase Bank", cardBin.getBankName());
        assertEquals("Visa", cardBin.getCardType());
        assertTrue(cardBin.getIsActive());
    }

    @Test
    void testCardBinWithParameterizedConstructor() {
        // Test creating CardBin with parameterized constructor
        CardBin newCardBin = new CardBin("555555", "Bank of America", "Mastercard");

        // Verify all fields are set correctly
        assertNull(newCardBin.getId()); // ID is auto-generated
        assertEquals("555555", newCardBin.getBin());
        assertEquals("Bank of America", newCardBin.getBankName());
        assertEquals("Mastercard", newCardBin.getCardType());
        assertTrue(newCardBin.getIsActive()); // Default value is true
    }

    @Test
    void testCardBinInactive() {
        // Test CardBin that is inactive
        cardBin.setId(2L);
        cardBin.setBin("378282");
        cardBin.setBankName("American Express");
        cardBin.setCardType("American Express");
        cardBin.setIsActive(false);

        // Verify all fields are set correctly
        assertEquals(2L, cardBin.getId());
        assertEquals("378282", cardBin.getBin());
        assertEquals("American Express", cardBin.getBankName());
        assertEquals("American Express", cardBin.getCardType());
        assertFalse(cardBin.getIsActive());
    }

    @Test
    void testCardBinEdgeCases() {
        // Test edge cases for BIN
        cardBin.setBin("000");
        assertEquals("000", cardBin.getBin());

        cardBin.setBin("999");
        assertEquals("999", cardBin.getBin());

        // Test edge cases for bank name
        cardBin.setBankName("A");
        assertEquals("A", cardBin.getBankName());

        cardBin.setBankName("Very Long Bank Name That Exceeds Normal Length Limits");
        assertEquals("Very Long Bank Name That Exceeds Normal Length Limits", cardBin.getBankName());

        // Test edge cases for card type
        cardBin.setCardType("X");
        assertEquals("X", cardBin.getCardType());

        cardBin.setCardType("Custom Card Type");
        assertEquals("Custom Card Type", cardBin.getCardType());
    }
}
