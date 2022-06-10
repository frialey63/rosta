package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class VolunteerDay extends AbstractDay {

    public VolunteerDay() {
        super();
    }

    public VolunteerDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    public VolunteerDay(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UUID.randomUUID().toString(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), userUuid);
    }

    @Override
    public String getColour() {
        return "#33ff33";
    }

}
