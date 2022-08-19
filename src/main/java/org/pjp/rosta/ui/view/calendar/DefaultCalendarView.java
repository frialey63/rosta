package org.pjp.rosta.ui.view.calendar;

import org.vaadin.stefan.fullcalendar.CalendarView;

import elemental.json.JsonObject;

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

    public void extendInitialOptions(JsonObject initialOptions) {
        // nothing to do
    }
}