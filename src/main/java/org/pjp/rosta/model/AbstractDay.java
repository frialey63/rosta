package org.pjp.rosta.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;

public abstract sealed class AbstractDay implements Comparable<AbstractDay>, PartOfDay permits AbsenceDay, Holiday, VolunteerDay {

    public static AbstractDay createDay(DayType dayType, LocalDate date, PartOfDay partOfDay, String userUuid) {
        return switch(dayType) {
        case ABSENCE -> new AbsenceDay(date, partOfDay, userUuid);
        case HOLIDAY -> new Holiday(date, partOfDay, userUuid);
        case VOLUNTARY -> new VolunteerDay(date, partOfDay, userUuid);
        };
    }

    public static DayType getDayType(String className) {
        return switch(className) {
        case "org.pjp.rosta.model.AbsenceDay" -> DayType.ABSENCE;
        case "org.pjp.rosta.model.Holiday" -> DayType.HOLIDAY;
        case "org.pjp.rosta.model.VolunteerDay" -> DayType.VOLUNTARY;
        default -> null;
        };
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("E dd MMMM");

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
    public int compareTo(AbstractDay other) {
        return date.compareTo(other.date);
    }

    @Override
    public String toString() {
        String str = date.format(FORMATTER);

        if (isAllDay()) {
            str += "";
        } else if (morning) {
            str += " Morning";
        } else if (afternoon) {
            str += " Afternoon";
        }

        return str;
    }

    public abstract String getColour();
}
