package org.pjp.rosta.bean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Rota {

    private final LocalDate rotaDate;	// a Monday

    private final RotaDay monday = new RotaDay(DayOfWeek.MONDAY);

    private final RotaDay tuesday = new RotaDay(DayOfWeek.TUESDAY);

    private final RotaDay wednesday = new RotaDay(DayOfWeek.WEDNESDAY);

    private final RotaDay thursday = new RotaDay(DayOfWeek.THURSDAY);

    private final RotaDay friday = new RotaDay(DayOfWeek.FRIDAY);

    private final RotaDay saturday = new RotaDay(DayOfWeek.SATURDAY);

    private final RotaDay sunday = new RotaDay(DayOfWeek.SUNDAY);

    private Map<DayOfWeek, RotaDay> map = new HashMap<>();

    {
        map.put(DayOfWeek.MONDAY, monday);
        map.put(DayOfWeek.TUESDAY, tuesday);
        map.put(DayOfWeek.WEDNESDAY, wednesday);
        map.put(DayOfWeek.THURSDAY, thursday);
        map.put(DayOfWeek.FRIDAY, friday);
        map.put(DayOfWeek.SATURDAY, saturday);
        map.put(DayOfWeek.SUNDAY, sunday);
    }

    public Rota(LocalDate rotaDate) {
        super();
        this.rotaDate = rotaDate;
    }

    public LocalDate getRotaDate() {
        return rotaDate;
    }

    public RotaDay getRotaDay(DayOfWeek dayOfWeek) {
        return map.get(dayOfWeek);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Rota [rotaDate=");
        builder.append(rotaDate);
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
