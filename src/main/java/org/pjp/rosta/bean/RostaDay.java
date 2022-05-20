package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import org.pjp.rosta.model.PartOfDay;

public class RostaDay {

    private final DayOfWeek dayofWeek;

    private final Set<String> morningUserUuids = new HashSet<>();

    private final Set<String> afternoonUserUuids = new HashSet<>();

    public RostaDay(DayOfWeek dayofWeek) {
        super();
        this.dayofWeek = dayofWeek;
    }

    public void addMorningUserUuid(String userUuid) {
        morningUserUuids.add(userUuid);
    }

    public void addAfternoonUserUuid(String userUuid) {
        afternoonUserUuids.add(userUuid);
    }

    public void addUserUuid(PartOfDay partOfDay, String userUuid) {
        if (partOfDay.isMorning()) {
            addMorningUserUuid(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            addAfternoonUserUuid(userUuid);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RostaDay [dayofWeek=");
        builder.append(dayofWeek);
        builder.append(", morningUserUuids=");
        builder.append(morningUserUuids);
        builder.append(", afternoonUserUuids=");
        builder.append(afternoonUserUuids);
        builder.append("]");
        return builder.toString();
    }

}
