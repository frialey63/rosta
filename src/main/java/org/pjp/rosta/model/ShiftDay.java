package org.pjp.rosta.model;

import java.time.DayOfWeek;

public class ShiftDay implements PartOfDay {

    private DayOfWeek dayOfWeek;

    private boolean morning;

    private boolean afternoon;

    public ShiftDay() {
        super();
    }

    public ShiftDay(DayOfWeek dayOfWeek) {
        super();
        this.dayOfWeek = dayOfWeek;
    }

    public ShiftDay(DayOfWeek dayOfWeek, boolean morning, boolean afternoon) {
        super();
        this.dayOfWeek = dayOfWeek;
        this.morning = morning;
        this.afternoon = afternoon;
    }

    public ShiftDay(DayOfWeek dayOfWeek, boolean allDay) {
        this(dayOfWeek, allDay, allDay);
    }

    @Override
	public boolean isMorning() {
        return morning;
    }

    public void setMorning(boolean morning) {
        this.morning = morning;
    }

    @Override
	public boolean isAfternoon() {
        return afternoon;
    }

    public void setAfternoon(boolean afternoon) {
        this.afternoon = afternoon;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
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
