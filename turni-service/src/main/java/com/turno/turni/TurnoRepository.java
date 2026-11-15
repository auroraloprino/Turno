package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
  List<Turno> findByDataBetweenOrderByDataAscInizioAsc(LocalDate dal, LocalDate al);

  List<Turno> findByUserIdAndDataBetweenOrderByDataAscInizioAsc(Long userId, LocalDate dal, LocalDate al);

  // inizia prima che l'altro finisca e finisce dopo che l'altro inizia (9-13 e 13-17 vanno bene)
  @Query("""
      select count(t) > 0 from Turno t
      where t.userId = :userId and t.data = :data
        and t.inizio < :fine and t.fine > :inizio
      """)
  boolean existsOverlap(@Param("userId") Long userId, @Param("data") LocalDate data,
      @Param("inizio") LocalTime inizio, @Param("fine") LocalTime fine);
}
