package com.turno.chat;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessaggioRepository extends MongoRepository<Messaggio, Long> {
    List<Messaggio> findByConversazioneIdOrderByTimestampAsc(Long conversazioneId);

    void deleteByConversazioneIdIn(List<Long> conversazioneIds);
}
