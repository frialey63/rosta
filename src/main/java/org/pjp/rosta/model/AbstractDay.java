package org.pjp.rosta.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.pjp.rosta.bean.Repeater;
import org.pjp.rosta.util.UuidStr;
import org.springframework.data.annotation.Id;

public abstract sealed class AbstractDay implements Comparable<AbstractDay>, PartOfDay permits AbsenceDay, Holiday, VolunteerDay {

    public static AbstractDay createDay(DayType dayType, LocalDate date, PartOfDay partOfDay, String userUuid) {
        return switch(dayType) {
        case ABSENCE -> new AbsenceDay(date, partOfDay, userUuid);
        case HOLIDAY -> new Holiday(date, partOfDay, userUuid);
        case VOLUNTEER -> new VolunteerDay(date, partOfDay, userUuid);
        };
    }

    public static List<AbstractDay> createVolunteerDays(LocalDate date, Repeater repeater, PartOfDay partOfDay, String userUuid) {
        String repeatUuid = UuidStr.random();

        return repeater.getSequence(date).stream().map(d -> new VolunteerDay(d, repeatUuid, partOfDay, userUuid)).collect(Collectors.toList());
    }

    public static DayType getDayType(String className) {
        assert "org.pjp.rosta.model".equals(AbsenceDay.class.getPackageName());
        assert "org.pjp.rosta.model".equals(Holiday.class.getPackageName());
        assert "org.pjp.rosta.model".equals(VolunteerDay.class.getPackageName());

        return switch(className) {
        case "org.pjp.rosta.model.AbsenceDay" -> DayType.ABSENCE;
        case "org.pjp.rosta.model.Holiday" -> DayType.HOLIDAY;
        case "org.pjp.rosta.model.VolunteerDay" -> DayType.VOLUNTEER;
        default -> null;
        };
    }

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("E dd MMMM");

    @Id
    private String uuid;

    private String repeatUuid;

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

    public String getRepeatUuid() {
        return repeatUuid;
    }

    public void setRepeatUuid(String repeatUuid) {
        this.repeatUuid = repeatUuid;
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

    public float getPartCount() {
        float count = 0;

        if (morning) {
            count += 0.5f;
        }

        if (afternoon) {
            count += 0.5f;
        }

        return count;
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

    public abstract int getDisplayOrder();

    public boolean overlapsWith(AbstractDay other) {
        if (this.isAllDay() || other.isAllDay()) {
            return true;
        }

        if (this.isMorning() && other.isMorning()) {
            return true;
        }

        if (this.isAfternoon() && other.isAfternoon()) {
            return true;
        }

        if ((this.isEvening() != null) && this.isEvening() && (other.isEvening() != null) && other.isEvening()) {
            return true;
        }

        return false;
    }
}
