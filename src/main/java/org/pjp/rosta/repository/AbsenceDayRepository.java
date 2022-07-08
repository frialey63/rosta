package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;

import org.pjp.rosta.model.AbsenceDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AbsenceDayRepository extends MongoRepository<AbsenceDay, String> {

    List<AbsenceDay> findAllByUserUuid(String userUuid);

    @Query(value = "{'userUuid':{ $eq: ?0}, 'date':{ $gte: ?1, $lte: ?2}}")
    List<AbsenceDay> findAllByUserUuidAndDateBetween(String userUuid, LocalDate dateStart, LocalDate dateEnd);

    @Query(value = "{'date':{ $gte: ?0, $lte: ?1}}")
    List<AbsenceDay> findAllByDateBetween(LocalDate dateStart, LocalDate dateEnd);

}
