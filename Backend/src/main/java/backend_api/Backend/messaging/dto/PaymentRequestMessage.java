package backend_api.Backend.messaging.dto;

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
    private String source;
    private Destination destination;
    private Payload payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        private String channel;
        private String eventName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        private String squad;
        private String topico;
        private String evento;
        private Cuerpo cuerpo;
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
}
