package org.pjp.rosta.bean;

public enum PartOfDay {
    MORNING, AFTERNOON, EVENING;

    @Override
    public String toString() {
        String str = super.toString();

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
