package com.turno.timbratura;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimbraturaRepository extends MongoRepository<Timbratura, Long> {
    List<Timbratura> findByUserIdOrderByTimestampDesc(Long userId);

    List<Timbratura> findAllByOrderByTimestampDesc();

    Optional<Timbratura> findTopByUserIdOrderByTimestampDesc(Long userId);

    void deleteByUserId(Long userId);
}
