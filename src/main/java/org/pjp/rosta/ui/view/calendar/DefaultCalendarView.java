package org.pjp.rosta.ui.view.calendar;

import org.vaadin.stefan.fullcalendar.CalendarView;

/**
 * Default implementation of a calendar view.
 */
public class DefaultCalendarView implements CalendarView {

    @Override
    public String getClientSideValue() {
        return "dayGridMonth";
    }

    @Override
    public String getName() {
        return "dayGridMonth";
    }
}