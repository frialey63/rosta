package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.pjp.rosta.model.Shift;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends MongoRepository<Shift, String> {

    List<Shift> findAllByUserUuid(String userUuid);

    Optional<Shift> findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(String userUuid, LocalDate fromDate);
}
