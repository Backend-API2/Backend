package backend_api.Backend.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/rabbitmq")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQHealthController {

    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;

    @GetMapping("/health")
    public ResponseEntity<?> checkRabbitMQHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Intentar obtener una conexión
            Connection connection = connectionFactory.createConnection();
            boolean isOpen = connection.isOpen();

            health.put("status", isOpen ? "UP" : "DOWN");
            health.put("connected", isOpen);

            if (isOpen) {
                connection.close();
                health.put("message", "Conexión exitosa con RabbitMQ");
            }

            log.info("RabbitMQ Health Check: {}", health);
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("connected", false);
            health.put("error", e.getMessage());
            health.put("message", "No se pudo conectar a RabbitMQ");

            log.error("Error en RabbitMQ Health Check: {}", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/queues")
    public ResponseEntity<?> listQueues() {
        Map<String, Object> response = new HashMap<>();

        try {
            Properties queueProperties = rabbitAdmin.getQueueProperties("core.payment.request.queue");

            response.put("core.payment.request.queue", queueProperties != null ? "EXISTS" : "NOT_FOUND");

            Properties queueProperties2 = rabbitAdmin.getQueueProperties("core.user.provider.data.queue");
            response.put("core.user.provider.data.queue", queueProperties2 != null ? "EXISTS" : "NOT_FOUND");

            Properties queueProperties3 = rabbitAdmin.getQueueProperties("core.event.response.queue");
            response.put("core.event.response.queue", queueProperties3 != null ? "EXISTS" : "NOT_FOUND");

            log.info("Queue Status: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            log.error("Error listando colas: {}", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getRabbitMQInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            Connection connection = connectionFactory.createConnection();

            info.put("host", connectionFactory.getHost());
            info.put("port", connectionFactory.getPort());
            info.put("virtualHost", connectionFactory.getVirtualHost());
            info.put("isOpen", connection.isOpen());

            connection.close();

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            info.put("error", e.getMessage());
            return ResponseEntity.status(500).body(info);
        }
    }
}
