package org.pjp.rosta.ui.view.rota;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.Env;
import org.pjp.rosta.bean.PartOfDay;
import org.pjp.rosta.bean.Rota;
import org.pjp.rosta.bean.RotaDay;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RotaService;
import org.pjp.rosta.ui.collab.Broadcaster;
import org.pjp.rosta.ui.component.CompactHorizontalLayout;
import org.pjp.rosta.ui.component.CompactVerticalLayout;
import org.pjp.rosta.ui.component.datepicker.MyDatePicker;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

@PermitAll
@PageTitle("Shop Rota")
@Route(value = "rota", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ShopRotaView extends AbstractView implements AfterNavigationObserver, ValueChangeListener<ValueChangeEvent<LocalDate>> {

    public static final File TMP = new File(Env.ROSTA_DATA,  "tmp");

    private static final String ROTA_S_DOCX = "rota_%s.docx";

    private static final int MAX_USERS_IN_PERIOD = 4;

    private static class GridBean {
        private String day;
        private String morning;
        private String afternoon;
        private String evening;

        public GridBean() {
            super();
        }

        public void set(PartOfDay partOfDay, String str) {
            switch(partOfDay) {
            case MORNING:
                setMorning(str);
                break;
            case AFTERNOON:
                setAfternoon(str);
                break;
            case EVENING:
                setEvening(str);
                break;
            }
        }

        public String getDay() {
            return day;
        }

        @SuppressWarnings("unused")
        public void setDay(String day) {
            this.day = day;
        }

        public String getMorning() {
            return morning;
        }

        public void setMorning(String morning) {
            this.morning = morning;
        }

        public String getAfternoon() {
            return afternoon;
        }

        public void setAfternoon(String afternoon) {
            this.afternoon = afternoon;
        }

        public String getEvening() {
            return evening;
        }

        public void setEvening(String evening) {
            this.evening = evening;
        }
    }

    private static final long serialVersionUID = 3386437553156944523L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopRotaView.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd_MMMM_yyyy");

    private static VerticalLayout getCentredLabel(String text) {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.setMargin(false);

        Label label = new Label(text);
        vl.setHorizontalComponentAlignment(Alignment.CENTER, label);
        vl.add(label);

        return vl;
    }

    private final Span labelSpan = new Span("Week beginning:");

    private final DatePicker datePicker = new MyDatePicker();

    private Registration registration;

    private LocalDate mondayOfRota;

    private final FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(null);

    private Map<DayOfWeek, Grid<GridBean>> dayGrids = new HashMap<>();

    private Map<DayOfWeek, Grid<GridBean>> grids = new HashMap<>();

    private  Map<DayOfWeek, List<GridBean>> allGridBeans = new HashMap<>();

    private Registration broadcasterRegistration;

    @Autowired
    private RotaService rotaService;

    @SuppressWarnings("unchecked")
    public ShopRotaView() {
        super();

        datePicker.setWeekNumbersVisible(true);
        registration = datePicker.addValueChangeListener(this);

        Span filler = new Span();
        setFlexGrow(1, filler);

        buttonWrapper.wrapComponent(new Button("Click to download"));

        HorizontalLayout hl = new CompactHorizontalLayout(labelSpan, datePicker, filler, buttonWrapper);
        hl.setVerticalComponentAlignment(Alignment.CENTER, labelSpan, datePicker, filler, buttonWrapper);
        hl.setAlignItems(Alignment.STRETCH);
        hl.setWidthFull();

        setHorizontalComponentAlignment(Alignment.START, hl);
        add(hl);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            HorizontalLayout layout = createGrid(dayOfWeek);

            dayGrids.put(dayOfWeek, (Grid<GridBean>) layout.getComponentAt(0));
            grids.put(dayOfWeek, (Grid<GridBean>) layout.getComponentAt(1));

            setHorizontalComponentAlignment(Alignment.START, layout);
            add(layout);
        }

        VerticalLayout vl = new CompactVerticalLayout();
        vl.getStyle().set("padding-bottom", "10px");

        add(vl);

        setMargin(false);
        setPadding(true);
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();

        broadcasterRegistration = Broadcaster.register(newMessage -> {
            ui.access(() -> {
                LOGGER.debug("received message {}", newMessage);

                LocalDate date = newMessage.date();

                switch (newMessage.messageType()) {
                case DAY_CREATE:
                case DAY_DELETE:
                    if (mondayOfRota.equals(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))) {
                        refresh(mondayOfRota);
                    }
                    break;
                case SHIFT_UPDATE:
                    if (!mondayOfRota.isBefore(date)) {
                        refresh(mondayOfRota);
                    }
                    break;
                }
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        datePicker.setValue(LocalDate.now());
    }

    @Override
    public void valueChanged(ValueChangeEvent<LocalDate> event) {
        mondayOfRota = event.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        registration.remove();
        datePicker.setValue(mondayOfRota);
        registration = datePicker.addValueChangeListener(this);

        refresh(mondayOfRota);
    }

    private void refresh(LocalDate date) {
        Rota rota = rotaService.buildRota(date);

        mapGridBeans(rota);
        populateGrids();

        String name = String.format(ROTA_S_DOCX, date.format(FORMATTER));
        buttonWrapper.setResource(new StreamResource(name, () -> {
            InputStream result = null;

            try {
                File tempFile = File.createTempFile("tmp-", ".tmp", TMP);
                tempFile.deleteOnExit();

                rotaService.writeRota(rota, tempFile);

                result = new FileInputStream(tempFile);
            } catch (IOException e) {
                LOGGER.error("error attempting to write rota to temporary file", e);
            }

            return result;
        }));
    }

    private void populateGrids() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            List<GridBean> temp = allGridBeans.get(dayOfWeek);

            dayGrids.get(dayOfWeek).setItems(temp);
            grids.get(dayOfWeek).setItems(temp);
        }
    }

    private void mapGridBeans(Rota rota) {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RotaDay rotaDay = rota.getRotaDay(dayOfWeek);

            List<GridBean> gridBeans = List.of(new GridBean(), new GridBean(), new GridBean(), new GridBean());

            for (PartOfDay partOfDay : PartOfDay.values()) {
                User[] users = rotaService.getUsers(rotaDay, partOfDay);

                for (int i = 0; i < Math.min(MAX_USERS_IN_PERIOD, users.length); i++) {
                    gridBeans.get(i).set(partOfDay, users[i].getDisplayName());
                }
            }

            allGridBeans.put(dayOfWeek, gridBeans);
        }
    }

    private HorizontalLayout createGrid(DayOfWeek dayOfWeek) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setPadding(false);
        hl.setMargin(false);
        hl.setSpacing(false);
        hl.setWidthFull();

        Grid<GridBean> dayGrid = new Grid<>(GridBean.class, false);
        dayGrid.setColumnReorderingAllowed(false);
        dayGrid.setAllRowsVisible(true);
        dayGrid.setWidth("20%");

        dayGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        dayGrid.addThemeVariants(GridVariant.LUMO_COMPACT);

        dayGrid.addColumn(GridBean::getDay).setHeader(dayOfWeek.getDisplayName(TextStyle.FULL, getLocale())).setWidth("100%").setFlexGrow(0);

        dayGrid.getColumns().forEach(c -> c.setSortable(false));

        Grid<GridBean> grid = new Grid<>(GridBean.class, false);
        grid.setColumnReorderingAllowed(false);
        grid.setAllRowsVisible(true);
        grid.setWidth("80%");

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        grid.addColumn(GridBean::getMorning).setHeader(getCentredLabel("Morning")).setWidth("40%").setFlexGrow(0);
        grid.addColumn(GridBean::getAfternoon).setHeader(getCentredLabel("Afternoon")).setWidth("40%").setFlexGrow(0);
        grid.addColumn(GridBean::getEvening).setHeader(getCentredLabel("Evening")).setWidth("20%").setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setSortable(false));

        hl.setVerticalComponentAlignment(Alignment.STRETCH, dayGrid, grid);
        hl.add(dayGrid, grid);

        return hl;
    }

}
