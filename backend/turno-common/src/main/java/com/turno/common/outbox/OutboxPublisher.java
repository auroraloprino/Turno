package com.turno.common.outbox;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final MongoOperations mongo;
    private final KafkaTemplate<String, Object> kafka;

    public OutboxPublisher(MongoOperations mongo, KafkaTemplate<String, Object> kafka) {
        this.mongo = mongo;
        this.kafka = kafka;
    }

    // se Kafka è giù gli eventi restano IN_ATTESA su Mongo e partono appena torna su
    @Scheduled(fixedDelay = 1000)
    public void pubblicaInAttesa() {
        var inAttesa = mongo.find(query(where("stato").is(EventoOutbox.IN_ATTESA))
                .with(Sort.by("id")).limit(100), EventoOutbox.class);
        for (EventoOutbox evento : inAttesa) {
            try {
                // aspetto la conferma del broker, più a lungo del delivery.timeout così un fail non parte dopo da solo
                kafka.send(evento.getTopic(), evento.getChiave(), evento.getPayload()).get(15, TimeUnit.SECONDS);
                evento.pubblicato();
                mongo.save(evento);
            } catch (Exception e) {
                evento.tentativoFallito();
                mongo.save(evento);
                log.warn("Evento outbox {} non pubblicato (tentativo {}), riprovo al prossimo giro",
                        evento.getId(), evento.getTentativi());
                // mi fermo qui per non mandare gli eventi successivi prima
                return;
            }
        }
    }
}
