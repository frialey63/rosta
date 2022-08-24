package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;

import org.pjp.rosta.model.VolunteerDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerDayRepository extends MongoRepository<VolunteerDay, String>, AbstractDayRepository<VolunteerDay> {

    List<VolunteerDay> findAllByUserUuid(String userUuid);

    @Query(value = "{'userUuid':{ $eq: ?0}, 'date':{ $gte: ?1, $lte: ?2}}")
    List<VolunteerDay> findAllByUserUuidAndDateBetween(String userUuid, LocalDate dateStart, LocalDate dateEnd);

    @Query(value = "{'date':{ $gte: ?0, $lte: ?1}}")
    List<VolunteerDay> findAllByDateBetween(LocalDate dateStart, LocalDate dateEnd);

    List<VolunteerDay> findAllByRepeatUuid(String repeatUuid);

}
