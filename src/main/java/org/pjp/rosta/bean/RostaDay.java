package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class RostaDay {

    private final DayOfWeek dayofWeek;

    private final List<String> morningUserUuids = new ArrayList<>();

    private final List<String> afternoonUserUuids = new ArrayList<>();

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
