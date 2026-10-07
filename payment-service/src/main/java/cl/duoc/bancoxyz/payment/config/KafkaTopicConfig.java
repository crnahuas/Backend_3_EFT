package cl.duoc.bancoxyz.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean NewTopic transaccionesTopic(@Value("${topics.transacciones-completadas}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
    @Bean NewTopic alertasTopic(@Value("${topics.alertas-seguridad}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
