package org.pjp.rosta.model;

import java.time.LocalDate;

import org.pjp.rosta.util.UuidStr;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class AbsenceDay extends AbstractDay {

    public AbsenceDay() {
        super();
    }

    public AbsenceDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    public AbsenceDay(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UuidStr.random(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), userUuid);
    }

    @Override
    public Boolean isEvening() {
        return null;
    }

    @Override
    public String getColour() {
        return "#ff3333";
    }

    @Override
    public int getDisplayOrder() {
        return 2;
    }

}
