package org.pjp.rosta.bean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Repeater(RepeatType repeatType, int count) {

    public enum RepeatType {
        NONE, WEEK, MONTH
    }

    public List<LocalDate> getSequence(LocalDate start) {
        return switch (repeatType) {
            case NONE -> Collections.emptyList();
            case WEEK -> Stream.iterate(start.plusWeeks(1), d -> d.plusWeeks(1)).limit(count).collect(Collectors.toList());
            case MONTH -> Stream.iterate(start.plusMonths(1), d -> d.plusMonths(1)).limit(count).collect(Collectors.toList());
        };
    }
}
