package com.turno.permessi;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PermessoRepository extends MongoRepository<Permesso, Long> {
    List<Permesso> findByUserIdOrderByDalDesc(Long userId);

    List<Permesso> findAllByOrderByDalDesc();

    List<Permesso> findByStatoOrderByDalDesc(StatoPermesso stato);

    void deleteByUserId(Long userId);
}
