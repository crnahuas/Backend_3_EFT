package cl.duoc.bancoxyz.customer.event;

import cl.duoc.bancoxyz.customer.domain.AlertaSeguridad;
import cl.duoc.bancoxyz.customer.repository.AlertaSeguridadRepository;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertaSeguridadConsumer {
    private static final Logger log = LoggerFactory.getLogger(AlertaSeguridadConsumer.class);
    private final ObjectMapper objectMapper;
    private final AlertaSeguridadRepository repository;

    public AlertaSeguridadConsumer(ObjectMapper objectMapper, AlertaSeguridadRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(topics = "${topics.alertas-seguridad}", groupId = "customer-security-monitor")
    public void consumir(String json) {
        try {
            var evento = objectMapper.readValue(json, AlertaSeguridadEvent.class);
            if (!repository.existsById(evento.eventoId())) {
                repository.save(new AlertaSeguridad(evento.eventoId(), evento.pagoId(), evento.rutCliente(),
                        evento.motivo(), evento.monto(), evento.fecha()));
            }
        } catch (Exception exception) {
            log.error("No fue posible procesar la alerta de seguridad", exception);
            throw new IllegalStateException("Alerta Kafka invalida", exception);
        }
    }
}
