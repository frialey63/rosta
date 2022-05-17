package org.pjp.rosta.model;

import java.time.DayOfWeek;

public class ShiftDay {

    private DayOfWeek dayofWeek;

    private boolean morning;

    private boolean afternoon;

    public ShiftDay() {
        super();
    }

    public ShiftDay(DayOfWeek dayofWeek) {
        super();
        this.dayofWeek = dayofWeek;
    }

    public ShiftDay(DayOfWeek dayofWeek, boolean morning, boolean afternoon) {
        super();
        this.dayofWeek = dayofWeek;
        this.morning = morning;
        this.afternoon = afternoon;
    }

    public ShiftDay(DayOfWeek dayofWeek, boolean allDay) {
        this(dayofWeek, allDay, allDay);
    }

    public boolean isMorning() {
        return morning;
    }

    public void setMorning(boolean morning) {
        this.morning = morning;
    }

    public boolean isAfternoon() {
        return afternoon;
    }

    public void setAfternoon(boolean afternoon) {
        this.afternoon = afternoon;
    }

    public DayOfWeek getDayofWeek() {
        return dayofWeek;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ShiftDay [morning=");
        builder.append(morning);
        builder.append(", afternoon=");
        builder.append(afternoon);
        builder.append("]");
        return builder.toString();
    }

}
