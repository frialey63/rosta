package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class AbsenceDay extends AbstractDay {

    public AbsenceDay() {
        super();
    }

    public AbsenceDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    public AbsenceDay(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UUID.randomUUID().toString(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), userUuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbsenceDay [toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
