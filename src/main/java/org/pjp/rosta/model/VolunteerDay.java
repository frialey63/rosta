package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class VolunteerDay extends AbstractDay {

    private boolean evening;

    public VolunteerDay() {
        super();
    }

    public VolunteerDay(String uuid, LocalDate date, boolean morning, boolean afternoon, boolean evening, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
        this.evening = evening;
    }

    public VolunteerDay(LocalDate date, PartOfDay partOfDay, String userUuid) {
        this(UUID.randomUUID().toString(), date, partOfDay.isMorning(), partOfDay.isAfternoon(), partOfDay.isEvening(), userUuid);
    }

    @Override
    public Boolean isEvening() {
        return evening;
    }

    public boolean isAllDay() {
        return super.isAllDay() && evening;
    }

    public float getPartCount() {
        float count = super.getPartCount();

        if (evening) {
            count += 0.5f;
        }

        return count;
    }

    @Override
    public String toString() {
        String str = getDate().format(FORMATTER);

        if (isAllDay()) {
            str += "";
        } else {
            str += String.join(" & ", "Morning", "Afternoon", "Evening");
        }

        return str;
    }

    @Override
    public String getColour() {
        return "#33ff33";
    }

    @Override
    public int getDisplayOrder() {
        return 3;
    }

}
