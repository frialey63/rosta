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

    public void addUserUuid(PartOfDay partOfDay, String userUuid) {
        if (partOfDay.isMorning()) {
            morningUserUuids.add(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.add(userUuid);
        }
    }

    public void removeUserUuid(PartOfDay partOfDay, String userUuid) {
        if (partOfDay.isMorning()) {
            morningUserUuids.remove(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.remove(userUuid);
        }
    }

    public String[] getUserUuids(org.pjp.rosta.bean.PartOfDay partOfDay) {
        switch (partOfDay) {
        case MORNING:
            return morningUserUuids.toArray(new String[0]);
        case AFTERNOON:
            return afternoonUserUuids.toArray(new String[0]);
        default:
            return new String[0];
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
