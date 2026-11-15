package com.turno.common.mongo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoOperations;

@AutoConfiguration
public class MongoAutoConfiguration {

    // @Lazy perché MongoTemplate a sua volta cerca questo callback quando nasce
    @Bean
    public IdNumerici idNumerici(@Lazy MongoOperations mongo) {
        return new IdNumerici(mongo);
    }
}
