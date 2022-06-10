package org.pjp.rosta.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

public abstract sealed class AbstractDay implements PartOfDay permits AbsenceDay, Holiday, VolunteerDay {

    public static AbstractDay createDay(DayType dayType, LocalDate date, PartOfDay partOfDay, String userUuid) {
        return switch(dayType) {
        case Absence -> new AbsenceDay(date, partOfDay, userUuid);
        case Holiday -> new Holiday(date, partOfDay, userUuid);
        case VolunteerDay -> new VolunteerDay(date, partOfDay, userUuid);
        };
    }

    public static DayType getDayType(String className) {
        return switch(className) {
        case "org.pjp.rosta.model.AbsenceDay" -> DayType.Absence;
        case "org.pjp.rosta.model.Holiday" -> DayType.Holiday;
        case "org.pjp.rosta.model.VolunteerDay" -> DayType.VolunteerDay;
        default -> null;
        };
    }

    @Id
    private String uuid;

    private LocalDate date;

    private boolean morning;

    private boolean afternoon;

    private String userUuid;

    public AbstractDay() {
        super();
    }

    public AbstractDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super();
        this.uuid = uuid;
        this.date = date;
        this.morning = morning;
        this.afternoon = afternoon;
        this.userUuid = userUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean isMorning() {
        return morning;
    }

    @Override
    public boolean isAfternoon() {
        return afternoon;
    }

    public boolean isAllDay() {
        return morning && afternoon;
    }

    public String getUserUuid() {
        return userUuid;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractDay [uuid=");
        builder.append(uuid);
        builder.append(", date=");
        builder.append(date);
        builder.append(", morning=");
        builder.append(morning);
        builder.append(", afternoon=");
        builder.append(afternoon);
        builder.append(", userUuid=");
        builder.append(userUuid);
        builder.append("]");
        return builder.toString();
    }

    public abstract String getColour();
}
