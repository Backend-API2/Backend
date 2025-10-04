// src/main/java/backend_api/Backend/messaging/config/QueueConfig.java
package backend_api.Backend.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    public static final String PAYMENT_COORDINATION_QUEUE = "payment.coordination.queue";
    public static final String PAYMENT_STATUS_UPDATE_QUEUE = "payment.status.update.queue";
    public static final String PAYMENT_COMMAND_QUEUE = "payment.command.queue";               // NUEVA
    public static final String PAYMENT_COORDINATION_CONFIRMED_QUEUE = "payment.coordination.confirmed.queue"; // NUEVA

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_DLQ = "payment.coordination.dlq";

    // DLX para mensajes que fallen
    public static final String PAYMENT_DLX = "payment.dlx";

    @Bean
    public Queue paymentCoordinationQueue() {
        return QueueBuilder.durable(PAYMENT_COORDINATION_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", "payment.coordination.dlq")
                .build();
    }

    @Bean
    public Queue paymentStatusUpdateQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue paymentCommandQueue() {
        return QueueBuilder.durable(PAYMENT_COMMAND_QUEUE).build();
    }

    @Bean
    public Queue paymentCoordinationConfirmedQueue() {
        return QueueBuilder.durable(PAYMENT_COORDINATION_CONFIRMED_QUEUE).build();
    }

    @Bean
    public Queue paymentDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentDeadLetterExchange() {
        return new TopicExchange(PAYMENT_DLX);
    }

    /* ---- Bindings ---- */

    @Bean
    public Binding paymentCoordinationBinding() {
        return BindingBuilder.bind(paymentCoordinationQueue())
                .to(paymentExchange())
                .with("payment.coordination");
    }

    @Bean
    public Binding paymentStatusBinding() {
        return BindingBuilder.bind(paymentStatusUpdateQueue())
                .to(paymentExchange())
                .with("payment.status.update");
    }

    @Bean
    public Binding paymentCommandBinding() {
        return BindingBuilder.bind(paymentCommandQueue())
                .to(paymentExchange())
                .with("payment.command.*");
    }

    // Para que podamos también CONSUMIR la confirmación que enviamos (útil si otro servicio la produce)
    @Bean
    public Binding paymentCoordinationConfirmedBinding() {
        return BindingBuilder.bind(paymentCoordinationConfirmedQueue())
                .to(paymentExchange())
                .with("payment.coordination.confirmed");
    }

    @Bean
    public Binding paymentDlqBinding() {
        return BindingBuilder.bind(paymentDeadLetterQueue())
                .to(paymentDeadLetterExchange())
                .with("payment.coordination.dlq");
    }
}