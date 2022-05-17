package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.pjp.rosta.model.Shift;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShiftRepository extends MongoRepository<Shift, UUID> {

    List<Shift> findAllByUserUuid(String userUuid);

    Optional<Shift> findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(String userUuid, LocalDate fromDate);
}
