package com.turno.timbratura;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimbraturaRepository extends JpaRepository<Timbratura, Long> {
    List<Timbratura> findByUserIdOrderByTimestampDesc(Long userId);

    List<Timbratura> findAllByOrderByTimestampDesc();

    Optional<Timbratura> findTopByUserIdOrderByTimestampDesc(Long userId);
}
