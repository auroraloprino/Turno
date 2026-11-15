package com.turno.permessi.mongo;

import org.bson.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class IdNumerici implements BeforeConvertCallback<ConIdNumerico> {

    private final MongoOperations mongo;

    public IdNumerici(@Lazy MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public ConIdNumerico onBeforeConvert(ConIdNumerico entity, String collection) {
        if (entity.getId() == null)
            entity.setId(prossimo(collection));
        return entity;
    }

    private long prossimo(String collection) {
        Document contatore = mongo.findAndModify(
                Query.query(Criteria.where("_id").is(collection)),
                new Update().inc("seq", 1L),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Document.class, "contatori");
        return ((Number) contatore.get("seq")).longValue();
    }
}
