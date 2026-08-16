package com.turno.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {
    List<Messaggio> findByConversazioneIdOrderByTimestampAsc(Long conversazioneId);
}
