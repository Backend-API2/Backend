package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Repository.CardBinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardValidationServiceImplTest {

    @Mock
    private CardBinRepository cardBinRepository;

    @InjectMocks
    private CardValidationServiceImpl cardValidationService;

    @BeforeEach
    void setUp() {
        // Setup is handled by MockitoExtension
    }

    @Test
    void testIsValidCardBin_ValidCard_ReturnsTrue() {
        // Given
        String cardNumber = "4111111111111111";
        when(cardBinRepository.existsByBinAndIsActiveTrue("411")).thenReturn(true);

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValidCardBin_InvalidCard_ReturnsFalse() {
        // Given
        String cardNumber = "9999999999999999";
        when(cardBinRepository.existsByBinAndIsActiveTrue("999")).thenReturn(false);

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidCardBin_NullCard_ReturnsFalse() {
        // Given
        String cardNumber = null;

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidCardBin_ShortCard_ReturnsFalse() {
        // Given
        String cardNumber = "12";

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidCardBin_EmptyCard_ReturnsFalse() {
        // Given
        String cardNumber = "";

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidCardBin_CardWithSpaces_ReturnsTrue() {
        // Given
        String cardNumber = "4111 1111 1111 1111";
        when(cardBinRepository.existsByBinAndIsActiveTrue("411")).thenReturn(true);

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValidCardBin_CardWithDashes_ReturnsTrue() {
        // Given
        String cardNumber = "4111-1111-1111-1111";
        when(cardBinRepository.existsByBinAndIsActiveTrue("411")).thenReturn(true);

        // When
        boolean result = cardValidationService.isValidCardBin(cardNumber);

        // Then
        assertTrue(result);
    }

    @Test
    void testExtractBin_ValidCard_ReturnsCorrectBin() {
        // Given
        String cardNumber = "4111111111111111";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("411", result);
    }

    @Test
    void testExtractBin_CardWithSpaces_ReturnsCorrectBin() {
        // Given
        String cardNumber = "4111 1111 1111 1111";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("411", result);
    }

    @Test
    void testExtractBin_CardWithDashes_ReturnsCorrectBin() {
        // Given
        String cardNumber = "4111-1111-1111-1111";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("411", result);
    }

    @Test
    void testExtractBin_Mastercard_ReturnsCorrectBin() {
        // Given
        String cardNumber = "5555555555554444";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("555", result);
    }

    @Test
    void testExtractBin_AmericanExpress_ReturnsCorrectBin() {
        // Given
        String cardNumber = "378282246310005";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("378", result);
    }

    @Test
    void testExtractBin_NullCard_ThrowsException() {
        // Given
        String cardNumber = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cardValidationService.extractBin(cardNumber);
        });
        assertEquals("Card number must have at least 3 digits", exception.getMessage());
    }

    @Test
    void testExtractBin_ShortCard_ThrowsException() {
        // Given
        String cardNumber = "12";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cardValidationService.extractBin(cardNumber);
        });
        assertEquals("Card number must have at least 3 digits", exception.getMessage());
    }

    @Test
    void testExtractBin_EmptyCard_ThrowsException() {
        // Given
        String cardNumber = "";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cardValidationService.extractBin(cardNumber);
        });
        assertEquals("Card number must have at least 3 digits", exception.getMessage());
    }

    @Test
    void testExtractBin_ExactlyThreeDigits_ReturnsCorrectBin() {
        // Given
        String cardNumber = "123";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("123", result);
    }

    @Test
    void testExtractBin_MixedFormatting_ReturnsCorrectBin() {
        // Given
        String cardNumber = "4 1 1 1-1 1 1 1 1 1 1 1 1 1 1";

        // When
        String result = cardValidationService.extractBin(cardNumber);

        // Then
        assertEquals("411", result);
    }
}
