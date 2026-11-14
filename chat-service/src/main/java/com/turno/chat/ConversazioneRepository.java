package com.turno.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversazioneRepository extends JpaRepository<Conversazione, Long> {
    @Query("SELECT c FROM Conversazione c JOIN c.partecipantiIds p WHERE p = :userId")
    List<Conversazione> findByPartecipanteId(Long userId);
}
