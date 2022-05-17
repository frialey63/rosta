package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;

// TODO implement me VolunteerDay
public class VolunteerDay {

    @Id
    private UUID id;

    private LocalDate date;

    private boolean morning;

    private boolean afternoon;

    private String userUuid;

}
