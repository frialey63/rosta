package org.pjp.rosta.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;

// TODO implement me Holiday
public class Holiday {

    @Id
    private UUID id;

    private LocalDate from;

    private LocalDate to;

    private String userUuid;

}
