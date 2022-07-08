package org.pjp.rosta.ui.util;

import com.vaadin.flow.component.datepicker.DatePicker;

public class MyDatePicker extends DatePicker {

    private static final long serialVersionUID = 8227123262251302723L;

    public MyDatePicker() {
        super();
        fixFirstDayOfWeek();
    }

    public MyDatePicker(String label) {
        super(label);
        fixFirstDayOfWeek();
    }

    private void fixFirstDayOfWeek() {
        setI18n(new DatePicker.DatePickerI18n() {
            private static final long serialVersionUID = 8035518406047591359L;

            @Override
            public int getFirstDayOfWeek() {
                return 1;
            }
        });
    }

}
