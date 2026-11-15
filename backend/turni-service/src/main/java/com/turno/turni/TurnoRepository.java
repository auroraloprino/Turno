package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TurnoRepository extends MongoRepository<Turno, Long> {

  // scrivo $gte e $lte perché il "Between" di Mongo esclude gli estremi
  @Query(value = "{ 'data': { $gte: ?0, $lte: ?1 } }", sort = "{ 'data': 1, 'inizio': 1 }")
  List<Turno> findRange(LocalDate dal, LocalDate al);

  @Query(value = "{ 'userId': ?0, 'data': { $gte: ?1, $lte: ?2 } }", sort = "{ 'data': 1, 'inizio': 1 }")
  List<Turno> findRangePerUtente(Long userId, LocalDate dal, LocalDate al);

  // inizia prima che l'altro finisca e finisce dopo che l'altro inizia (9-13 e 13-17 vanno bene)
  @Query(value = "{ 'userId': ?0, 'data': ?1, 'inizio': { $lt: ?3 }, 'fine': { $gt: ?2 } }", exists = true)
  boolean existsOverlap(Long userId, LocalDate data, LocalTime inizio, LocalTime fine);

  @Query("{ 'userId': { $in: ?0 }, 'data': { $gte: ?1, $lte: ?2 } }")
  List<Turno> findPerUtentiNelPeriodo(Collection<Long> userIds, LocalDate dal, LocalDate al);

  void deleteByUserId(Long userId);
}
