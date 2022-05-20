package org.pjp.rosta.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

public abstract class AbstractDay implements PartOfDay {

    @Id
    private String uuid;

    private LocalDate date;

    private boolean morning;

    private boolean afternoon;

    private String userUuid;

    public AbstractDay(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super();
        this.uuid = uuid;
        this.date = date;
        this.morning = morning;
        this.afternoon = afternoon;
        this.userUuid = userUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean isMorning() {
        return morning;
    }

    @Override
    public boolean isAfternoon() {
        return afternoon;
    }

    public String getUserUuid() {
        return userUuid;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractDay [uuid=");
        builder.append(uuid);
        builder.append(", date=");
        builder.append(date);
        builder.append(", morning=");
        builder.append(morning);
        builder.append(", afternoon=");
        builder.append(afternoon);
        builder.append(", userUuid=");
        builder.append(userUuid);
        builder.append("]");
        return builder.toString();
    }

}
