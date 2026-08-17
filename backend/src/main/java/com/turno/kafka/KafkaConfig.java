package com.turno.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic permessoTopic() {
        return new NewTopic("permesso-stato", 1, (short) 1);
    }

    @Bean
    public NewTopic timbraturaTopic() {
        return new NewTopic("timbratura-audit", 1, (short) 1);
    }
}
