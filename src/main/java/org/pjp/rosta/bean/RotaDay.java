package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RotaDay {

    private final DayOfWeek dayofWeek;

    private final Set<String> morningUserUuids = new HashSet<>();

    private final Set<String> afternoonUserUuids = new HashSet<>();

    private final Set<String> eveningUserUuids = new HashSet<>();

    private Map<PartOfDay, Set<String>> map = new HashMap<>();

    {
        map.put(PartOfDay.MORNING, morningUserUuids);
        map.put(PartOfDay.AFTERNOON, afternoonUserUuids);
        map.put(PartOfDay.EVENING, eveningUserUuids);
    }

    public RotaDay(DayOfWeek dayofWeek) {
        super();
        this.dayofWeek = dayofWeek;
    }

    public void addUserUuid(org.pjp.rosta.model.PartOfDay partOfDay, String userUuid) {
        if (partOfDay.isMorning()) {
            morningUserUuids.add(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.add(userUuid);
        }

        if (Boolean.TRUE == partOfDay.isEvening()) {
            eveningUserUuids.add(userUuid);
        }
    }

    public void removeUserUuid(org.pjp.rosta.model.PartOfDay partOfDay, String userUuid) {
        if (partOfDay.isMorning()) {
            morningUserUuids.remove(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.remove(userUuid);
        }

        if (Boolean.TRUE == partOfDay.isEvening()) {
            eveningUserUuids.remove(userUuid);
        }
    }

    public Set<String> getUserUuids(PartOfDay partOfDay) {
        return Collections.unmodifiableSet(map.get(partOfDay));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RotaDay [dayofWeek=");
        builder.append(dayofWeek);
        builder.append(", morningUserUuids=");
        builder.append(morningUserUuids);
        builder.append(", afternoonUserUuids=");
        builder.append(afternoonUserUuids);
        builder.append(", eveningUserUuids=");
        builder.append(eveningUserUuids);
        builder.append("]");
        return builder.toString();
    }

}
