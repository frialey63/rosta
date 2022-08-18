package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;

import org.pjp.rosta.model.Holiday;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends MongoRepository<Holiday, String>, AbstractDayRepository<Holiday> {

    List<Holiday> findAllByUserUuid(String userUuid);

    List<Holiday> findAllByUserUuidAndDate(String userUuid, LocalDate date);

    @Query(value = "{'userUuid':{ $eq: ?0}, 'date':{ $gte: ?1, $lte: ?2}}")
    List<Holiday> findAllByUserUuidAndDateBetween(String userUuid, LocalDate dateStart, LocalDate dateEnd);

    @Query(value = "{'date':{ $gte: ?0, $lte: ?1}}")
    List<Holiday> findAllByDateBetween(LocalDate dateStart, LocalDate dateEnd);

}
