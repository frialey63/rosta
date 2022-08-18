package org.pjp.rosta.repository;

import java.time.LocalDate;
import java.util.List;

import org.pjp.rosta.model.AbstractDay;

public interface AbstractDayRepository<T extends AbstractDay> {

    List<T> findAllByUserUuidAndDate(String userUuid, LocalDate date);

}