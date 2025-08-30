# DER - Módulo de Pagos y Facturación 


erDiagram
    PAYMENT {
        Long id PK
        String payment_intent_id
        Long user_id
        Long provider_id
        Long solicitud_id
        Long cotizacion_id
        BigDecimal amount_subtotal
        BigDecimal taxes
        BigDecimal fees
        BigDecimal amount_total
        String currency
        PaymentStatus status
        String gateway_txn_id
        Long payment_method_id FK
        LocalDateTime created_at
        LocalDateTime updated_at
        LocalDateTime captured_at
        LocalDateTime expired_at
        String metadata
    }
    
    PAYMENT_METHOD {
        Long id PK
        PaymentMethodType type
    }
    
    CREDIT_CARD_PAYMENT {
        Long id PK
        String card_network
        String last4Digits
        String holder_name
        Integer expiration_month
        Integer expiration_year
    }
    
    DEBIT_CARD_PAYMENT {
        Long id PK
        String card_network
        String last4Digits
        String holder_name
        Integer expiration_month
        Integer expiration_year
        String bank_name
        String cbu
    }
    
    BANK_TRANSFER_PAYMENT {
        Long id PK
        String cbu
        String bank_name
        String alias
    }
    
    WALLET_PAYMENT {
        Long id PK
        String wallet_provider
        String alias
    }
    
    PAYMENT_ATTEMPT {
        Long id PK
        Long payment_id FK
        Integer attempt_number
        PaymentStatus status
        String response_code
        String gateway_response_code
        LocalDateTime created_at
    }
    
    PAYMENT_EVENT {
        Long id PK
        Long payment_id FK
        String type
        String payload
        LocalDateTime created_at
        String actor
    }
    
    INVOICE {
        Long id PK
        Long payment_id FK
        String invoice_number
        LocalDateTime issue_date
        BigDecimal total_amount
        InvoiceStatus status
        InvoiceType type
        Long user_id
        Long provider_id
        String legal_fields
        String pdf_url
    }
    
    INVOICE_LINE {
        Long id PK
        Long invoice_id FK
        String description
        Integer quantity
        BigDecimal unit_price
        BigDecimal subtotal
    }
    
    REFUND {
        Long id PK
        Long paymend_id FK
        BigDecimal amount
        String reason
        RefundStatus status
        String gateway_refund_id
        LocalDateTime created_at
    }
    
    DISPUTE {
        Long id PK
        Long payment_id FK
        DisputeStatus status
        String evidences
        BigDecimal amount_provisioned
        LocalDateTime created_at
    }
    
    RECONCILIATION {
        Long id PK
        LocalDateTime date
        String gateway_batch_id
        BigDecimal net_amount
        BigDecimal fees
        ReconciliationStatus status
    }

    %% Relationships principales
    PAYMENT ||--o{ PAYMENT_ATTEMPT : "has"
    PAYMENT ||--o{ PAYMENT_EVENT : "has_events"
    PAYMENT_METHOD ||--o{ PAYMENT : "used_in"
    PAYMENT ||--o{ INVOICE : "generates"
    INVOICE ||--o{ INVOICE_LINE : "contains"
    PAYMENT ||--o{ REFUND : "can_have"
    PAYMENT ||--o{ DISPUTE : "can_have"
    PAYMENT ||--o{ RECONCILIATION : "reconciled_in"
    
    %% Herencia de PaymentMethod
    PAYMENT_METHOD ||--|| CREDIT_CARD_PAYMENT : "extends"
    PAYMENT_METHOD ||--|| DEBIT_CARD_PAYMENT : "extends"
    PAYMENT_METHOD ||--|| BANK_TRANSFER_PAYMENT : "extends"
    PAYMENT_METHOD ||--|| WALLET_PAYMENT : "extends"
```
