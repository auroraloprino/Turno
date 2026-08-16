package com.turno.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversazioneRepository extends JpaRepository<Conversazione, Long> {
    @Query("SELECT c FROM Conversazione c JOIN c.partecipanti p WHERE p.id = :userId")
    List<Conversazione> findByPartecipanteId(Long userId);
}
