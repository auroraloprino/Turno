package com.turno.permesso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermessoRepository extends JpaRepository<Permesso, Long> {
    List<Permesso> findByUserIdOrderByDalDesc(Long userId);

    List<Permesso> findAllByOrderByDalDesc();

    List<Permesso> findByStatoOrderByDalDesc(StatoPermesso stato);
}
