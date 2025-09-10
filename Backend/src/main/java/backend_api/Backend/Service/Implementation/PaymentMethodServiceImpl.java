package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.*;
import backend_api.Backend.Entity.payment.types.*;
import backend_api.Backend.DTO.payment.SelectPaymentMethodRequest;
import backend_api.Backend.Repository.*;
import backend_api.Backend.Service.Interface.PaymentMethodService;
import backend_api.Backend.Service.Interface.CardValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {
    
    @Autowired
    private CashPaymentRepository cashPaymentRepository;

    @Autowired
    private TestCardRepository testCardRepository;
    
    @Autowired
    private MercadoPagoPaymentRepository mercadoPagoRepository;
    
    @Autowired
    private PaypalPaymentRepository paypalPaymentRepository;
    
    @Autowired
    private CreditCardPaymentRepository creditCardRepository;
    
    @Autowired
    private DebitCardPaymentRepository debitCardRepository;
    
    @Autowired
    private CardValidationService cardValidationService;
    
    @Override
    public PaymentMethod createPaymentMethod(SelectPaymentMethodRequest request) {
        PaymentMethodType type = PaymentMethodType.valueOf(request.getPaymentMethodType());
        
        switch (type) {
            case CASH:
                return createCashMethod(request);
            case MERCADO_PAGO:
                return createMercadoPagoMethod(request);
            case PAYPAL:
                return createPaypalMethod(request);
            case CREDIT_CARD:
                return createCreditCardMethod(request);
            case DEBIT_CARD:
                return createDebitCardMethod(request);
            case BANK_TRANSFER:
                return createBasicMethod(type);
            default:
                throw new RuntimeException("Tipo de método de pago no soportado: " + type);
        }
    }
    
    private PaymentMethod createCashMethod(SelectPaymentMethodRequest request) {
        CashPayment cashMethod = new CashPayment();
        cashMethod.setBranchCode(request.getBranchCode());
        cashMethod.setBranchName(request.getBranchName());
        cashMethod.setBranchAddress(request.getBranchAddress());
        
        cashMethod.setPaymentCode("CASH-" + System.currentTimeMillis());
        
        cashMethod.setExpirationDate(java.time.LocalDateTime.now().plusDays(7));
        
        return cashPaymentRepository.save(cashMethod);
    }
    
    private PaymentMethod createMercadoPagoMethod(SelectPaymentMethodRequest request) {
        MercadoPagoPayment mpMethod = new MercadoPagoPayment();
        mpMethod.setMercadoPagoUserId(request.getMercadoPagoUserId());
        mpMethod.setAccessToken(request.getAccessToken());
        
        return mercadoPagoRepository.save(mpMethod);
    }
    
    private PaymentMethod createPaypalMethod(SelectPaymentMethodRequest request) {
        PaypalPayment paypalMethod = new PaypalPayment();
        paypalMethod.setPaypalEmail(request.getPaypalEmail());
        
        return paypalPaymentRepository.save(paypalMethod);
    }

    private PaymentMethod createCreditCardMethod(SelectPaymentMethodRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new RuntimeException("Número de tarjeta es requerido");
        }
        if (!cardValidationService.isValidCardBin(request.getCardNumber())) {
            throw new RuntimeException("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.");
        }

        CreditCardPayment creditCard = new CreditCardPayment();
        creditCard.setType(PaymentMethodType.CREDIT_CARD);

        String cleanCardNumber = request.getCardNumber().replaceAll("[\\s-]", "");
        if (cleanCardNumber.length() >= 4) {
            creditCard.setLast4Digits(cleanCardNumber.substring(cleanCardNumber.length() - 4));
        }
        creditCard.setHolder_name(request.getCardHolderName());
        creditCard.setExpiration_month(request.getExpirationMonth());
        creditCard.setExpiration_year(request.getExpirationYear());

        String bin = cardValidationService.extractBin(request.getCardNumber());
        creditCard.setCard_network(determineCardNetwork(bin));

        TestCard tc = findTestCardOrNull(request.getCardNumber(), request.getCvv());
        if (tc != null) {
            creditCard.setTestCard(tc);
        } else {
        }

        return creditCardRepository.save(creditCard);
    }

    private PaymentMethod createDebitCardMethod(SelectPaymentMethodRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new RuntimeException("Número de tarjeta es requerido");
        }
        if (!cardValidationService.isValidCardBin(request.getCardNumber())) {
            throw new RuntimeException("Los primeros 3 dígitos de la tarjeta no son válidos. Pago rechazado.");
        }

        DebitCardPayment debitCard = new DebitCardPayment();
        debitCard.setType(PaymentMethodType.DEBIT_CARD);

        String cleanCardNumber = request.getCardNumber().replaceAll("[\\s-]", "");
        if (cleanCardNumber.length() >= 4) {
            debitCard.setLast4Digits(cleanCardNumber.substring(cleanCardNumber.length() - 4));
        }
        debitCard.setHolder_name(request.getCardHolderName());
        debitCard.setExpiration_month(request.getExpirationMonth());
        debitCard.setExpiration_year(request.getExpirationYear());
        debitCard.setBank_name(request.getBankName());
        debitCard.setCbu(request.getCbu());

        String bin = cardValidationService.extractBin(request.getCardNumber());
        debitCard.setCard_network(determineCardNetwork(bin));

        TestCard tc = findTestCardOrNull(request.getCardNumber(), request.getCvv());
        if (tc != null) {
            debitCard.setTestCard(tc);
        }

        return debitCardRepository.save(debitCard);
    }
    
    private String determineCardNetwork(String bin) {
        if (bin.startsWith("4")) {
            return "VISA";
        } else if (bin.startsWith("5") || bin.startsWith("2")) {
            return "MASTERCARD";
        } else if (bin.startsWith("3")) {
            return "AMERICAN_EXPRESS";
        } else {
            return "UNKNOWN";
        }
    }
    
    private PaymentMethod createBasicMethod(PaymentMethodType type) {
        BasicPaymentMethod basicMethod = new BasicPaymentMethod();
        basicMethod.setType(type);
        

        return basicMethod;
    }
    
    @Override
    public PaymentMethod getPaymentMethodById(Long id) {
        
        if (cashPaymentRepository.existsById(id)) {
            return cashPaymentRepository.findById(id).orElse(null);
        }
        
        if (mercadoPagoRepository.existsById(id)) {
            return mercadoPagoRepository.findById(id).orElse(null);
        }
        
        if (paypalPaymentRepository.existsById(id)) {
            return paypalPaymentRepository.findById(id).orElse(null);
        }
        
  
        return null;
    }
    
    @Override
    public void deletePaymentMethod(Long id) {
        
        if (cashPaymentRepository.existsById(id)) {
            cashPaymentRepository.deleteById(id);
            return;
        }
        
        if (mercadoPagoRepository.existsById(id)) {
            mercadoPagoRepository.deleteById(id);
            return;
        }
        
        if (paypalPaymentRepository.existsById(id)) {
            paypalPaymentRepository.deleteById(id);
            return;
        }
        
        throw new RuntimeException("PaymentMethod con ID " + id + " no encontrado");
    }
    
    private static class BasicPaymentMethod extends PaymentMethod {
    }

    private String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TestCard findTestCardOrNull(String pan, String cvv) {
        if (pan == null || cvv == null) return null;
        String panHash = sha256(pan.replaceAll("[\\s-]", ""));
        String cvvHash = sha256(cvv.trim());
        return testCardRepository
                .findByPanSha256AndCvvSha256AndIsActiveTrue(panHash, cvvHash)
                .orElse(null);
    }
}
