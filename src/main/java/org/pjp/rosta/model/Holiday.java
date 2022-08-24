package org.pjp.rosta.model;

import java.time.LocalDate;

import org.pjp.rosta.util.UuidStr;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class Holiday extends AbstractDay {

    public Holiday() {
        super();
    }

    public Holiday(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    public Holiday(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UuidStr.random(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), userUuid);
    }

    @Override
    public Boolean isEvening() {
        return null;
    }

    @Override
    public String getColour() {
        return "#3333ff";
    }

    @Override
    public int getDisplayOrder() {
        return 1;
    }

}
