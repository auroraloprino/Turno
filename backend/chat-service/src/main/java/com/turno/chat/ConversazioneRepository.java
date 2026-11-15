package com.turno.chat;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ConversazioneRepository extends MongoRepository<Conversazione, Long> {
    // su un array, Mongo trova i documenti che contengono quel valore
    @Query("{ 'partecipantiIds': ?0 }")
    List<Conversazione> findByPartecipanteId(Long userId);
}
