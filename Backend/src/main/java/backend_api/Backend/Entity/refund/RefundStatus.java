// backend_api/Backend/Entity/refund/RefundStatus.java
package backend_api.Backend.Entity.refund;

public enum RefundStatus {
    PENDING,         // creado por el usuario, esperando al merchant
    APPROVED,        // (opcional transitorio) merchant aceptó, previo a ejecutar
    DECLINED,        // merchant rechazó
    PARTIAL_REFUND,  // ejecutado parcial
    TOTAL_REFUND,    // ejecutado total
    FAILED           // intentado y falló en gateway
}