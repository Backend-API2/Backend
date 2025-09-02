package backend_api.Backend.Entity.cotizacion;

public enum CotizacionStatus {
    DRAFT,      // Borrador - recién creada
    PENDING,    
    SENT,       
    VIEWED,     
    APPROVED,   
    REJECTED,   
    EXPIRED,    
    CONVERTED,  // Convertida a factura/pago
    CANCELED,    
    REVISED     // Nueva versión creada
}
