package backend_api.Backend.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestMessage {
    private String messageId;
    private String timestamp;
    
    @Deprecated
    private String source;
    
    private Destination destination;
    private Payload payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        private String topic;
        private String eventName;
        
        @Deprecated
        private String channel;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        private String squad;
        private String topico;
        private String evento;
        private Cuerpo cuerpo;
        
        // Nuevo formato de matching
        private String generatedAt;
        private Pago pago;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cuerpo {
        private String idCorrelacion;
        private Long idUsuario;
        private Long idPrestador;
        private Long idSolicitud;
        private BigDecimal montoSubtotal;
        private BigDecimal impuestos;
        private BigDecimal comisiones;
        private String moneda;
        private String metodoPreferido;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pago {
        private String idCorrelacion;
        
        @JsonProperty("idUsuario")
        private Long idUsuario;
        
        @JsonProperty("idPrestador")
        private Long idPrestador;
        
        @JsonProperty("idSolicitud")
        private Long idSolicitud;
        private BigDecimal montoSubtotal;
        private BigDecimal impuestos;
        private BigDecimal comisiones;
        private String moneda;
        private String metodoPreferido;
    }
}
