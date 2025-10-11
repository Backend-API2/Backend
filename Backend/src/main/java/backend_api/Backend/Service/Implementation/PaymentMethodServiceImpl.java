package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.payment.*;
import backend_api.Backend.Entity.payment.types.*;
import backend_api.Backend.DTO.payment.SelectPaymentMethodRequest;
import backend_api.Backend.Repository.*;
import backend_api.Backend.Repository.PaymentMethodRepository;
import backend_api.Backend.Service.Interface.PaymentMethodService;
import backend_api.Backend.Service.Interface.CardValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {
    
    @Autowired
    private CashPaymentRepository cashPaymentRepository;
    
    @Autowired
    private MercadoPagoPaymentRepository mercadoPagoRepository;
    
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
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
        
        return paymentMethodRepository.save(creditCard);
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
        
        return paymentMethodRepository.save(debitCard);
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
        // Buscar en todos los repositorios específicos
        PaymentMethod method = cashPaymentRepository.findById(id).orElse(null);
        if (method != null) return method;
        
        method = mercadoPagoRepository.findById(id).orElse(null);
        if (method != null) return method;
        
        
        method = paymentMethodRepository.findById(id).orElse(null);
        if (method != null) return method;
        
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
        
        
        if (paymentMethodRepository.existsById(id)) {
            paymentMethodRepository.deleteById(id);
            return;
        }
        
        throw new RuntimeException("PaymentMethod con ID " + id + " no encontrado");
    }
    
    private static class BasicPaymentMethod extends PaymentMethod {
    }
}
