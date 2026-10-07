package cl.duoc.bancoxyz.customer.event;

import cl.duoc.bancoxyz.customer.service.ClienteService;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransaccionConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransaccionConsumer.class);
    private final ObjectMapper objectMapper;
    private final ClienteService clienteService;

    public TransaccionConsumer(ObjectMapper objectMapper, ClienteService clienteService) {
        this.objectMapper = objectMapper;
        this.clienteService = clienteService;
    }

    @KafkaListener(topics = "${topics.transacciones-completadas}", groupId = "${spring.kafka.consumer.group-id:customer-service}")
    public void consumir(String json) {
        try {
            var evento = objectMapper.readValue(json, TransaccionCompletadaEvent.class);
            clienteService.registrarActividad(evento.rutCliente(), evento.fecha());
        } catch (Exception exception) {
            log.error("No fue posible procesar el evento de transaccion", exception);
            throw new IllegalStateException("Evento Kafka invalido", exception);
        }
    }
}
