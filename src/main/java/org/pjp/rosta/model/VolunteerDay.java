package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class VolunteerDay extends AbstractDay {

    public VolunteerDay() {
        super();
        // TODO Auto-generated constructor stub
    }

    public VolunteerDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    public VolunteerDay(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UUID.randomUUID().toString(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), userUuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VolunteerDay [toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
