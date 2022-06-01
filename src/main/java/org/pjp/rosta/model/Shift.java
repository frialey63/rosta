package org.pjp.rosta.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Shift {

    @Id
    private String uuid;

    private LocalDate fromDate;	// a Monday, TODO assert this...

    private String userUuid;

    private ShiftDay monday = new ShiftDay(DayOfWeek.MONDAY, true);

    private ShiftDay tuesday = new ShiftDay(DayOfWeek.TUESDAY, true);

    private ShiftDay wednesday = new ShiftDay(DayOfWeek.WEDNESDAY, true);

    private ShiftDay thursday = new ShiftDay(DayOfWeek.THURSDAY, true);

    private ShiftDay friday = new ShiftDay(DayOfWeek.FRIDAY, true);

    private ShiftDay saturday = new ShiftDay(DayOfWeek.SATURDAY);

    private ShiftDay sunday = new ShiftDay(DayOfWeek.SUNDAY);

    private transient Map<DayOfWeek, ShiftDay> map = new TreeMap<>();

    {
        map.put(DayOfWeek.MONDAY, monday);
        map.put(DayOfWeek.TUESDAY, tuesday);
        map.put(DayOfWeek.WEDNESDAY, wednesday);
        map.put(DayOfWeek.THURSDAY, thursday);
        map.put(DayOfWeek.FRIDAY, friday);
        map.put(DayOfWeek.SATURDAY, saturday);
        map.put(DayOfWeek.SUNDAY, sunday);
    }

    public Shift() {
        super();
    }

    public Shift(String uuid, LocalDate fromDate, String userUuid) {
        super();
        this.uuid = uuid;
        this.fromDate = fromDate;
        this.userUuid = userUuid;
    }

    public Shift(LocalDate fromDate, String userUuid) {
        this(UUID.randomUUID().toString(), fromDate, userUuid);
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public ShiftDay getShiftDay(DayOfWeek dayOfWeek) {
        return map.get(dayOfWeek);
    }

    public Iterator<ShiftDay> getShiftDayIterator() {
        return map.values().iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Shift [uuid=");
        builder.append(uuid);
        builder.append(", fromDate=");
        builder.append(fromDate);
        builder.append(", userUuid=");
        builder.append(userUuid);
        builder.append("]");
        return builder.toString();
    }

}
