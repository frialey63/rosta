package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pjp.rosta.model.PartOfDayWithOpener;

public class RostaDay {

    private final DayOfWeek dayofWeek;

    private final Set<String> openerUserUuids = new HashSet<>();

    private final Set<String> morningUserUuids = new HashSet<>();

    private final Set<String> afternoonUserUuids = new HashSet<>();

    private Map<PartOfDay, Set<String>> map = new HashMap<>();

    {
        map.put(PartOfDay.OPENER, openerUserUuids);
        map.put(PartOfDay.MORNING, morningUserUuids);
        map.put(PartOfDay.AFTERNOON, afternoonUserUuids);
    }

    public RostaDay(DayOfWeek dayofWeek) {
        super();
        this.dayofWeek = dayofWeek;
    }

    public void addUserUuid(org.pjp.rosta.model.PartOfDay partOfDay, String userUuid) {
        if (partOfDay instanceof PartOfDayWithOpener withOpener) {
            if (withOpener.isOpener()) {
                openerUserUuids.add(userUuid);
            }
        }

        if (partOfDay.isMorning()) {
            morningUserUuids.add(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.add(userUuid);
        }
    }

    public void removeUserUuid(org.pjp.rosta.model.PartOfDay partOfDay, String userUuid) {
        if (partOfDay instanceof PartOfDayWithOpener withOpener) {
            if (withOpener.isOpener()) {
                openerUserUuids.remove(userUuid);
            }
        }

        if (partOfDay.isMorning()) {
            morningUserUuids.remove(userUuid);
        }

        if (partOfDay.isAfternoon()) {
            afternoonUserUuids.remove(userUuid);
        }
    }

    public Set<String> getUserUuids(PartOfDay partOfDay) {
        return Collections.unmodifiableSet(map.get(partOfDay));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RostaDay [dayofWeek=");
        builder.append(dayofWeek);
        builder.append(", openerUserUuids=");
        builder.append(openerUserUuids);
        builder.append(", morningUserUuids=");
        builder.append(morningUserUuids);
        builder.append(", afternoonUserUuids=");
        builder.append(afternoonUserUuids);
        builder.append("]");
        return builder.toString();
    }

}
