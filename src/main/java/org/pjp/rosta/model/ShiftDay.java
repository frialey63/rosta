package org.pjp.rosta.model;

import java.time.DayOfWeek;

public class ShiftDay implements PartOfDayWithOpener {

    private DayOfWeek dayOfWeek;

    private boolean opener;

    private boolean morning;

    private boolean afternoon;

    public ShiftDay() {
        super();
    }

    public ShiftDay(DayOfWeek dayOfWeek) {
        super();
        this.dayOfWeek = dayOfWeek;
    }

    public ShiftDay(DayOfWeek dayOfWeek, boolean opener, boolean morning, boolean afternoon) {
        super();
        this.dayOfWeek = dayOfWeek;
        this.opener = opener;
        this.morning = morning;
        this.afternoon = afternoon;
    }

    public ShiftDay(DayOfWeek dayOfWeek, boolean opener, boolean allDay) {
        this(dayOfWeek, opener, allDay, allDay);
    }

    public boolean isOpener() {
        return opener;
    }

    public void setOpener(boolean opener) {
        this.opener = opener;
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

    public boolean isAllDay() {
        return morning && afternoon;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ShiftDay [opener=");
        builder.append(opener);
        builder.append(", morning=");
        builder.append(morning);
        builder.append(", afternoon=");
        builder.append(afternoon);
        builder.append("]");
        return builder.toString();
    }

}
