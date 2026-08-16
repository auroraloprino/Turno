package com.turno.turno;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByDataBetweenOrderByDataAscInizioAsc(LocalDate dal, LocalDate al);
    List<Turno> findByUserIdAndDataBetweenOrderByDataAscInizioAsc(Long userId, LocalDate dal, LocalDate al);
}
