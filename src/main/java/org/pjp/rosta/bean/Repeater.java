package org.pjp.rosta.bean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Repeater(RepeatType repeatType, int count) {

    public enum RepeatType {
        NONE, DAILY, WEEKLY, MONTHLY
    }

    public List<LocalDate> getSequence(LocalDate start) {
        return switch (repeatType) {
            case NONE -> Collections.emptyList();
            case DAILY -> Stream.iterate(start, d -> d.plusDays(1)).limit(1 + count).collect(Collectors.toList());
            case WEEKLY -> Stream.iterate(start, d -> d.plusWeeks(1)).limit(1 + count).collect(Collectors.toList());
            case MONTHLY -> Stream.iterate(start, d -> d.plusMonths(1)).limit(1 + count).collect(Collectors.toList());
        };
    }
}
