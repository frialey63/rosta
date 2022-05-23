package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Rosta {

    private final LocalDate rostaDate;	// a Monday

    private final RostaDay monday = new RostaDay(DayOfWeek.MONDAY);

    private final RostaDay tuesday = new RostaDay(DayOfWeek.TUESDAY);

    private final RostaDay wednesday = new RostaDay(DayOfWeek.WEDNESDAY);

    private final RostaDay thursday = new RostaDay(DayOfWeek.THURSDAY);

    private final RostaDay friday = new RostaDay(DayOfWeek.FRIDAY);

    private final RostaDay saturday = new RostaDay(DayOfWeek.SATURDAY);

    private final RostaDay sunday = new RostaDay(DayOfWeek.SUNDAY);

    private Map<DayOfWeek, RostaDay> map = new HashMap<>();

    {
        map.put(DayOfWeek.MONDAY, monday);
        map.put(DayOfWeek.TUESDAY, tuesday);
        map.put(DayOfWeek.WEDNESDAY, wednesday);
        map.put(DayOfWeek.THURSDAY, thursday);
        map.put(DayOfWeek.FRIDAY, friday);
        map.put(DayOfWeek.SATURDAY, saturday);
        map.put(DayOfWeek.SUNDAY, sunday);
    }

    public Rosta(LocalDate rostaDate) {
        super();
        this.rostaDate = rostaDate;
    }

    public LocalDate getRostaDate() {
        return rostaDate;
    }

    public RostaDay getRostaDay(DayOfWeek dayOfWeek) {
        return map.get(dayOfWeek);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Rosta [rostaDate=");
        builder.append(rostaDate);
        builder.append(", monday=");
        builder.append(monday);
        builder.append(", tuesday=");
        builder.append(tuesday);
        builder.append(", wednesday=");
        builder.append(wednesday);
        builder.append(", thursday=");
        builder.append(thursday);
        builder.append(", friday=");
        builder.append(friday);
        builder.append(", saturday=");
        builder.append(saturday);
        builder.append(", sunday=");
        builder.append(sunday);
        builder.append("]");
        return builder.toString();
    }

}
