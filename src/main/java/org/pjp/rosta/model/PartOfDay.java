package org.pjp.rosta.model;

import java.util.ArrayList;
import java.util.List;

public interface PartOfDay {

    boolean isMorning();

    boolean isAfternoon();

    Boolean isEvening();

    boolean isAllDay();

    public static String getTitle(PartOfDay partOfDay) {
        if (partOfDay.isEvening() == null) {
            if (partOfDay.isAllDay()) {
                return "All Day";
            } else if (partOfDay.isMorning()) {
                return "Morning";
            } else if (partOfDay.isAfternoon()) {
                return "Afternoon";
            }

            throw new IllegalStateException();
        } else {
            if (partOfDay.isAllDay()) {
                return "All Day";
            } else {
                List<String> str = new ArrayList<>();

                if (partOfDay.isMorning()) {
                    str.add("Morning");
                }

                if (partOfDay.isAfternoon()) {
                    str.add("Afternoon");
                }

                if (partOfDay.isEvening()) {
                    str.add("Evening");
                }

                return String.join(" & ", str);
            }
        }
    }

    public static String abbreviate(String str) {
        if (str.contains("&")) {
            return str.replace("Morning", "Mrng").replace("Afternoon", "Aftn").replace("Evening", "Evng");
        }

        return str;
    }

}