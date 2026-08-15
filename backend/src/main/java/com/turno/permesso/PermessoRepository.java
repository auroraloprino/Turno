package com.turno.permesso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermessoRepository extends JpaRepository<Permesso, Long> {
    List<Permesso> findByUserIdOrderByDalDesc(Long userId);
    List<Permesso> findAllByOrderByDalDesc();
    List<Permesso> findByStatoOrderByDalDesc(StatoPermesso stato);
}
