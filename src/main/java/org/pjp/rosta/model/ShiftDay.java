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

    @Override
    public Boolean isEvening() {
        return null;
    }

    public boolean isAllDay() {
        return morning && afternoon;
    }

    public boolean isWorking() {
        return morning || afternoon;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
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

    public String getColour() {
        return isAllDay() ? "#33ff33" : "#55ff55";
    }

}
