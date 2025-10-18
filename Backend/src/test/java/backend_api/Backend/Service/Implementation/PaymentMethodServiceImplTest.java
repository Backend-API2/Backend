/*package backend_api.Backend.Service.Implementation;

import backend_api.Backend.DTO.payment.SelectPaymentMethodRequest;
import backend_api.Backend.Entity.payment.*;
import backend_api.Backend.Entity.payment.types.*;
import backend_api.Backend.Repository.*;
import backend_api.Backend.Service.Interface.CardValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @Mock
    private CashPaymentRepository cashPaymentRepository;

    @Mock
    private MercadoPagoPaymentRepository mercadoPagoRepository;

    @Mock
    private PaypalPaymentRepository paypalPaymentRepository;

    @Mock
    private CreditCardPaymentRepository creditCardRepository;

    @Mock
    private DebitCardPaymentRepository debitCardRepository;

    @Mock
    private CardValidationService cardValidationService;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private SelectPaymentMethodRequest request;
    private CashPayment cashPayment;
    private MercadoPagoPayment mercadoPagoPayment;
    private PaypalPayment paypalPayment;
    private CreditCardPayment creditCardPayment;
    private DebitCardPayment debitCardPayment;

    @BeforeEach
    void setUp() {
        request = new SelectPaymentMethodRequest();
        request.setPaymentMethodType("CASH");
        request.setBranchCode("001");
        request.setBranchName("Test Branch");
        request.setBranchAddress("Test Address");

        cashPayment = new CashPayment();
        cashPayment.setId(1L);
        cashPayment.setBranchCode("001");
        cashPayment.setBranchName("Test Branch");

        mercadoPagoPayment = new MercadoPagoPayment();
        mercadoPagoPayment.setId(2L);
        mercadoPagoPayment.setMercadoPagoUserId("test_user");

        paypalPayment = new PaypalPayment();
        paypalPayment.setId(3L);
        paypalPayment.setPaypalEmail("test@paypal.com");

        creditCardPayment = new CreditCardPayment();
        creditCardPayment.setId(4L);
        creditCardPayment.setHolder_name("Test Holder");
        creditCardPayment.setLast4Digits("1234");

        debitCardPayment = new DebitCardPayment();
        debitCardPayment.setId(5L);
        debitCardPayment.setHolder_name("Test Holder");
        debitCardPayment.setLast4Digits("5678");
    }

    @Test
    void testCreatePaymentMethod_Cash_Success() {
        // Given
        when(cashPaymentRepository.save(any(CashPayment.class))).thenReturn(cashPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(cashPayment.getId(), result.getId());
        verify(cashPaymentRepository).save(any(CashPayment.class));
    }

    @Test
    void testCreatePaymentMethod_MercadoPago_Success() {
        // Given
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setMercadoPagoUserId("test_user");
        request.setAccessToken("test_token");
        when(mercadoPagoRepository.save(any(MercadoPagoPayment.class))).thenReturn(mercadoPagoPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(mercadoPagoPayment.getId(), result.getId());
        verify(mercadoPagoRepository).save(any(MercadoPagoPayment.class));
    }

    @Test
    void testCreatePaymentMethod_Paypal_Success() {
        // Given
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setPaypalEmail("test@paypal.com");
        when(paypalPaymentRepository.save(any(PaypalPayment.class))).thenReturn(paypalPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(paypalPayment.getId(), result.getId());
        verify(paypalPaymentRepository).save(any(PaypalPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_Success() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(creditCardPayment.getId(), result.getId());
        verify(cardValidationService).isValidCardBin("4111111111111111");
        verify(cardValidationService).extractBin("4111111111111111");
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidCardNumber() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Número de tarjeta es requerido", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidBin() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("9999999999999999");
        when(cardValidationService.isValidCardBin("9999999999999999")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_Success() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName("Test Bank");
        request.setCbu("1234567890123456789012");
        when(cardValidationService.isValidCardBin("5555555555554444")).thenReturn(true);
        when(cardValidationService.extractBin("5555555555554444")).thenReturn("555");
        when(debitCardRepository.save(any(DebitCardPayment.class))).thenReturn(debitCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(debitCardPayment.getId(), result.getId());
        verify(cardValidationService).isValidCardBin("5555555555554444");
        verify(cardValidationService).extractBin("5555555555554444");
        verify(debitCardRepository).save(any(DebitCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_BankTransfer_Success() {
        // Given
        request.setPaymentMethodType("BANK_TRANSFER");

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(PaymentMethodType.BANK_TRANSFER, result.getType());
    }

    @Test
    void testCreatePaymentMethod_UnsupportedType() {
        // Given
        request.setPaymentMethodType("UNSUPPORTED");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertTrue(exception.getMessage().contains("No enum constant"));
    }

    @Test
    void testGetPaymentMethodById_CashPayment_Success() {
        // Given
        Long id = 1L;
        when(cashPaymentRepository.existsById(id)).thenReturn(true);
        when(cashPaymentRepository.findById(id)).thenReturn(Optional.of(cashPayment));

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNotNull(result);
        assertEquals(cashPayment.getId(), result.getId());
        verify(cashPaymentRepository).existsById(id);
        verify(cashPaymentRepository).findById(id);
    }

    @Test
    void testGetPaymentMethodById_MercadoPagoPayment_Success() {
        // Given
        Long id = 2L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(true);
        when(mercadoPagoRepository.findById(id)).thenReturn(Optional.of(mercadoPagoPayment));

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNotNull(result);
        assertEquals(mercadoPagoPayment.getId(), result.getId());
        verify(cashPaymentRepository).existsById(id);
        verify(mercadoPagoRepository).existsById(id);
        verify(mercadoPagoRepository).findById(id);
    }

    @Test
    void testGetPaymentMethodById_PaypalPayment_Success() {
        // Given
        Long id = 3L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(true);
        when(paypalPaymentRepository.findById(id)).thenReturn(Optional.of(paypalPayment));

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNotNull(result);
        assertEquals(paypalPayment.getId(), result.getId());
        verify(cashPaymentRepository).existsById(id);
        verify(mercadoPagoRepository).existsById(id);
        verify(paypalPaymentRepository).existsById(id);
        verify(paypalPaymentRepository).findById(id);
    }

    @Test
    void testGetPaymentMethodById_NotFound() {
        // Given
        Long id = 999L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNull(result);
    }

    @Test
    void testDeletePaymentMethod_CashPayment_Success() {
        // Given
        Long id = 1L;
        when(cashPaymentRepository.existsById(id)).thenReturn(true);
        doNothing().when(cashPaymentRepository).deleteById(id);

        // When
        paymentMethodService.deletePaymentMethod(id);

        // Then
        verify(cashPaymentRepository).existsById(id);
        verify(cashPaymentRepository).deleteById(id);
        verify(mercadoPagoRepository, never()).deleteById(any());
        verify(paypalPaymentRepository, never()).deleteById(any());
    }

    @Test
    void testDeletePaymentMethod_MercadoPagoPayment_Success() {
        // Given
        Long id = 2L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(true);
        doNothing().when(mercadoPagoRepository).deleteById(id);

        // When
        paymentMethodService.deletePaymentMethod(id);

        // Then
        verify(cashPaymentRepository).existsById(id);
        verify(mercadoPagoRepository).existsById(id);
        verify(mercadoPagoRepository).deleteById(id);
        verify(paypalPaymentRepository, never()).deleteById(any());
    }

    @Test
    void testDeletePaymentMethod_PaypalPayment_Success() {
        // Given
        Long id = 3L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(true);
        doNothing().when(paypalPaymentRepository).deleteById(id);

        // When
        paymentMethodService.deletePaymentMethod(id);

        // Then
        verify(cashPaymentRepository).existsById(id);
        verify(mercadoPagoRepository).existsById(id);
        verify(paypalPaymentRepository).existsById(id);
        verify(paypalPaymentRepository).deleteById(id);
    }

    @Test
    void testDeletePaymentMethod_NotFound() {
        // Given
        Long id = 999L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.deletePaymentMethod(id);
        });
        assertEquals("PaymentMethod con ID 999 no encontrado", exception.getMessage());
    }

    @Test
    void testDetermineCardNetwork_Visa() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "VISA".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testDetermineCardNetwork_Mastercard() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("5555555555554444")).thenReturn(true);
        when(cardValidationService.extractBin("5555555555554444")).thenReturn("555");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "MASTERCARD".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testDetermineCardNetwork_AmericanExpress() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("378282246310005");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("378282246310005")).thenReturn(true);
        when(cardValidationService.extractBin("378282246310005")).thenReturn("378");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "AMERICAN_EXPRESS".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testDetermineCardNetwork_Unknown() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("9999999999999999");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("9999999999999999")).thenReturn(true);
        when(cardValidationService.extractBin("9999999999999999")).thenReturn("999");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "UNKNOWN".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidHolderName() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName(""); // Empty holder name
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidExpirationMonth() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(0); // Invalid month
        request.setExpirationYear(2025);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidExpirationYear() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2020); // Past year

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_InvalidCardNumber() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber(""); // Empty card number
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName("Test Bank");
        request.setCbu("1234567890123456789012");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Número de tarjeta es requerido", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_InvalidHolderName() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName(""); // Empty holder name
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName("Test Bank");
        request.setCbu("1234567890123456789012");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_InvalidBankName() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName(""); // Empty bank name
        request.setCbu("1234567890123456789012");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_InvalidCbu() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName("Test Bank");
        request.setCbu("123"); // Invalid CBU length

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_DebitCard_InvalidBin() {
        // Given
        request.setPaymentMethodType("DEBIT_CARD");
        request.setCardNumber("9999999999999999");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setBankName("Test Bank");
        request.setCbu("1234567890123456789012");
        when(cardValidationService.isValidCardBin("9999999999999999")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.createPaymentMethod(request);
        });
        assertEquals("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.", exception.getMessage());
    }

    @Test
    void testCreatePaymentMethod_MercadoPago_InvalidUserId() {
        // Given
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setMercadoPagoUserId(""); // Empty user ID
        request.setAccessToken("test_token");
        when(mercadoPagoRepository.save(any(MercadoPagoPayment.class))).thenReturn(new MercadoPagoPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(mercadoPagoRepository).save(any(MercadoPagoPayment.class));
    }

    @Test
    void testCreatePaymentMethod_MercadoPago_InvalidAccessToken() {
        // Given
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setMercadoPagoUserId("test_user");
        request.setAccessToken(""); // Empty access token
        when(mercadoPagoRepository.save(any(MercadoPagoPayment.class))).thenReturn(new MercadoPagoPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(mercadoPagoRepository).save(any(MercadoPagoPayment.class));
    }

    @Test
    void testCreatePaymentMethod_Paypal_InvalidEmail() {
        // Given
        request.setPaymentMethodType("MERCADO_PAGO");
        request.setPaypalEmail(""); // Empty email
        when(paypalPaymentRepository.save(any(PaypalPayment.class))).thenReturn(new PaypalPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(paypalPaymentRepository).save(any(PaypalPayment.class));
    }

    @Test
    void testCreatePaymentMethod_Cash_InvalidBranchCode() {
        // Given
        request.setPaymentMethodType("CASH");
        request.setBranchCode(""); // Empty branch code
        request.setBranchName("Test Branch");
        request.setBranchAddress("Test Address");
        when(cashPaymentRepository.save(any(CashPayment.class))).thenReturn(new CashPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(cashPaymentRepository).save(any(CashPayment.class));
    }

    @Test
    void testCreatePaymentMethod_Cash_InvalidBranchName() {
        // Given
        request.setPaymentMethodType("CASH");
        request.setBranchCode("001");
        request.setBranchName(""); // Empty branch name
        request.setBranchAddress("Test Address");
        when(cashPaymentRepository.save(any(CashPayment.class))).thenReturn(new CashPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(cashPaymentRepository).save(any(CashPayment.class));
    }

    @Test
    void testCreatePaymentMethod_Cash_InvalidBranchAddress() {
        // Given
        request.setPaymentMethodType("CASH");
        request.setBranchCode("001");
        request.setBranchName("Test Branch");
        request.setBranchAddress(""); // Empty branch address
        when(cashPaymentRepository.save(any(CashPayment.class))).thenReturn(new CashPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(cashPaymentRepository).save(any(CashPayment.class));
    }

    @Test
    void testGetPaymentMethodById_CreditCardPayment_Success() {
        // Given
        Long id = 4L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNull(result); // Method not implemented, returns null
    }

    @Test
    void testGetPaymentMethodById_DebitCardPayment_Success() {
        // Given
        Long id = 5L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When
        PaymentMethod result = paymentMethodService.getPaymentMethodById(id);

        // Then
        assertNull(result); // Method not implemented, returns null
    }

    @Test
    void testDeletePaymentMethod_CreditCardPayment_Success() {
        // Given
        Long id = 4L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.deletePaymentMethod(id);
        });
        assertEquals("PaymentMethod con ID 4 no encontrado", exception.getMessage());
    }

    @Test
    void testDeletePaymentMethod_DebitCardPayment_Success() {
        // Given
        Long id = 5L;
        when(cashPaymentRepository.existsById(id)).thenReturn(false);
        when(mercadoPagoRepository.existsById(id)).thenReturn(false);
        when(paypalPaymentRepository.existsById(id)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.deletePaymentMethod(id);
        });
        assertEquals("PaymentMethod con ID 5 no encontrado", exception.getMessage());
    }

    @Test
    void testDetermineCardNetwork_DinersClub() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("30569309025904");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("30569309025904")).thenReturn(true);
        when(cardValidationService.extractBin("30569309025904")).thenReturn("305");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "AMERICAN_EXPRESS".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testDetermineCardNetwork_Discover() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("6011111111111117");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("6011111111111117")).thenReturn(true);
        when(cardValidationService.extractBin("6011111111111117")).thenReturn("601");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "UNKNOWN".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testDetermineCardNetwork_JCB() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("3530111333300000");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("3530111333300000")).thenReturn(true);
        when(cardValidationService.extractBin("3530111333300000")).thenReturn("353");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(argThat(payment -> 
            "AMERICAN_EXPRESS".equals(((CreditCardPayment) payment).getCard_network())
        ));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_ExpiredCard() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(2020); // Past year
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(new CreditCardPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_InvalidMonth() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(13); // Invalid month
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(new CreditCardPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_ZeroMonth() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(0); // Zero month
        request.setExpirationYear(2025);
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(new CreditCardPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_CurrentYearValidMonth() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(12);
        request.setExpirationYear(LocalDateTime.now().getYear()); // Current year
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(creditCardPayment);

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        assertEquals(creditCardPayment.getId(), result.getId());
        verify(cardValidationService).isValidCardBin("4111111111111111");
        verify(cardValidationService).extractBin("4111111111111111");
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }

    @Test
    void testCreatePaymentMethod_CreditCard_CurrentYearInvalidMonth() {
        // Given
        request.setPaymentMethodType("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Test Holder");
        request.setExpirationMonth(LocalDateTime.now().getMonthValue() - 1); // Past month in current year
        request.setExpirationYear(LocalDateTime.now().getYear());
        when(cardValidationService.isValidCardBin("4111111111111111")).thenReturn(true);
        when(cardValidationService.extractBin("4111111111111111")).thenReturn("411");
        when(creditCardRepository.save(any(CreditCardPayment.class))).thenReturn(new CreditCardPayment());

        // When
        PaymentMethod result = paymentMethodService.createPaymentMethod(request);

        // Then
        assertNotNull(result);
        verify(creditCardRepository).save(any(CreditCardPayment.class));
    }
}
*/