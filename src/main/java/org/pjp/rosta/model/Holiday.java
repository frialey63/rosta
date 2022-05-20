package org.pjp.rosta.model;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Holiday extends AbstractDay {

    public Holiday(String uuid, LocalDate date, boolean morning, boolean afternoon, String userUuid) {
        super(uuid, date, morning, afternoon, userUuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Holiday [toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
