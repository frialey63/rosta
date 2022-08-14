package org.pjp.rosta.ui.component.fc;

import javax.validation.constraints.NotNull;

import org.vaadin.stefan.fullcalendar.FullCalendar;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import elemental.json.JsonObject;

@NpmPackage(value = "tippy.js", version = "6.2.3")
@Tag("full-calendar-with-tooltip")
@JsModule("./src/full-calendar-with-tooltip.js")
@CssImport("tippy.js/dist/tippy.css")
@CssImport("tippy.js/themes/light.css")
public class FullCalendarWithTooltip extends FullCalendar {

    private static final long serialVersionUID = -8166115252470543847L;

    public FullCalendarWithTooltip() {
        super();
    }

    public FullCalendarWithTooltip(int entryLimit) {
        super(entryLimit);
    }

    public FullCalendarWithTooltip(@NotNull JsonObject initialOptions) {
        super(initialOptions);
    }

    /**
     * Enumeration of possible options, that can be applied to this calendar instance to have an effect on the client side.
     * This list does not contain all options, but the most common used ones.
     * <br><br>
     * Please refer to the FullCalendar client library documentation for possible options:
     * https://fullcalendar.io/docs
     */
    public enum Option {
        FIRST_DAY("firstDay"),
        HEIGHT("height"),
        LOCALE("locale"),
        SELECTABLE("selectable"),
        WEEK_NUMBERS("weekNumbers"),
        NOW_INDICATOR("nowIndicator"),
        NAV_LINKS("navLinks"),
        BUSINESS_HOURS("businessHours"),
        TIMEZONE("timeZone"),
        SNAP_DURATION("snapDuration"),
        SLOT_MIN_TIME("slotMinTime"),
        SLOT_MAX_TIME("slotMaxTime"),
        FIXED_WEEK_COUNT("fixedWeekCount"),
        WEEKENDS("weekends"),
        HEADER_TOOLBAR("headerToolbar"),
        FOOTER_TOOLBAR("footerToolbar"),
        COLUMN_HEADER("columnHeader"),
        WEEK_NUMBERS_WITHIN_DAYS("weekNumbersWithinDays"),
        ENTRY_DURATION_EDITABLE("eventDurationEditable"),
        ENTRY_RESIZABLE_FROM_START("eventResizableFromStart"),
        ENTRY_START_EDITABLE("eventStartEditable"),
        ENTRY_TIME_FORMAT("eventTimeFormat"),
        EDITABLE("editable");

        private final String optionKey;

        Option(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }

}