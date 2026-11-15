package com.turno.orchestratore.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    // se un servizio è giù riprovo ogni 5 secondi per un minuto, poi lo segno nel log e vado avanti
    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                (record, e) -> log.error("Cancellazione non completata per {} dopo tutti i tentativi", record.value(), e),
                new FixedBackOff(5000L, 12));
    }
}
