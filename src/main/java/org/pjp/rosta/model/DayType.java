package org.pjp.rosta.model;

public enum DayType {
    HOLIDAY, ABSENCE, VOLUNTEER;


    @Override
    public String toString() {
        String str = super.toString();

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}