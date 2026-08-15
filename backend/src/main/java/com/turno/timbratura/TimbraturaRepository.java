package com.turno.timbratura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimbraturaRepository extends JpaRepository<Timbratura, Long> {
    List<Timbratura> findByUserIdOrderByTimestampDesc(Long userId);
    List<Timbratura> findAllByOrderByTimestampDesc();
    Optional<Timbratura> findTopByUserIdOrderByTimestampDesc(Long userId);
}
