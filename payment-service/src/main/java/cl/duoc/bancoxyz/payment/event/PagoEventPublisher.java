package cl.duoc.bancoxyz.payment.event;

import cl.duoc.bancoxyz.payment.domain.Pago;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PagoEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PagoEventPublisher.class);
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final String transaccionesTopic;
    private final String alertasTopic;

    public PagoEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper objectMapper,
                              @Value("${topics.transacciones-completadas}") String transaccionesTopic,
                              @Value("${topics.alertas-seguridad}") String alertasTopic) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
        this.transaccionesTopic = transaccionesTopic;
        this.alertasTopic = alertasTopic;
    }

    public void transaccionCompletada(Pago pago) {
        enviar(transaccionesTopic, pago.getId().toString(), new TransaccionCompletadaEvent(
                UUID.randomUUID(), pago.getId(), pago.getRutCliente(), pago.getTipo(), pago.getCuentaOrigen(),
                pago.getCuentaDestino(), pago.getMonto(), pago.getMoneda(), Instant.now()));
    }

    public void alerta(Pago pago, String motivo) {
        enviar(alertasTopic, pago.getId().toString(), new AlertaSeguridadEvent(
                UUID.randomUUID(), pago.getId(), pago.getRutCliente(), motivo, pago.getMonto(), Instant.now()));
    }

    private void enviar(String topic, String key, Object evento) {
        try {
            kafka.send(topic, key, objectMapper.writeValueAsString(evento));
        } catch (Exception exception) {
            log.error("No fue posible publicar evento en {}", topic, exception);
        }
    }
}
