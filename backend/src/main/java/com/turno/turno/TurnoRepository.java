package com.turno.turno;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByDataBetweenOrderByDataAscInizioAsc(LocalDate dal, LocalDate al);

    List<Turno> findByUserIdAndDataBetweenOrderByDataAscInizioAsc(Long userId, LocalDate dal, LocalDate al);
}
