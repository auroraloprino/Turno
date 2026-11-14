package com.turno.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {
    List<Messaggio> findByConversazioneIdOrderByTimestampAsc(Long conversazioneId);
}
