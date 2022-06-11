package org.pjp.rosta.ui.view.rosta;

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

import org.pjp.rosta.bean.PartOfDay;
import org.pjp.rosta.bean.Rosta;
import org.pjp.rosta.bean.RostaDay;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

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

@PageTitle("The Shop Rota")
@Route(value = "rota", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class RostaView extends VerticalLayout implements AfterNavigationObserver, ValueChangeListener<ValueChangeEvent<LocalDate>> {

    static class GridBean {
        private String day;
        private String opener;
        private String morning;
        private String afternoon;

        public GridBean() {
            super();
        }

        public GridBean(String opener, String morning, String afternoon) {
            super();
            this.opener = opener;
            this.morning = morning;
            this.afternoon = afternoon;
        }

        public void set(PartOfDay partOfDay, String str) {
            switch(partOfDay) {
            case OPENER:
                setOpener(str);
                break;
            case MORNING:
                setMorning(str);
                break;
            case AFTERNOON:
                setAfternoon(str);
                break;
            }
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getOpener() {
            return opener;
        }

        public void setOpener(String opener) {
            this.opener = opener;
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
    }

    private static final long serialVersionUID = 3386437553156944523L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaView.class);

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

    private final DatePicker datePicker = new DatePicker();

    private Registration registration;

    private final FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(null);

    private Map<DayOfWeek, Grid<GridBean>> dayGrids = new HashMap<>();

    private Map<DayOfWeek, Grid<GridBean>> grids = new HashMap<>();

    private  Map<DayOfWeek, List<GridBean>> allGridBeans = new HashMap<>();

    @Autowired
    private RostaService rostaService;

    @SuppressWarnings("unchecked")
    public RostaView() {
        registration = datePicker.addValueChangeListener(this);

        Span filler = new Span();
        setFlexGrow(1, filler);

        buttonWrapper.wrapComponent(new Button("Click to download"));

        HorizontalLayout hl = new CompactHorizontalLayout(labelSpan, datePicker, filler, buttonWrapper);
        hl.setVerticalComponentAlignment(Alignment.CENTER, labelSpan, datePicker, filler, buttonWrapper);
        hl.setAlignItems(Alignment.STRETCH);
        hl.setWidth("98%");

        setHorizontalComponentAlignment(Alignment.START, hl);
        add(hl);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            HorizontalLayout layout = createGrid(dayOfWeek);

            dayGrids.put(dayOfWeek, (Grid<GridBean>) layout.getComponentAt(0));
            grids.put(dayOfWeek, (Grid<GridBean>) layout.getComponentAt(1));

            setHorizontalComponentAlignment(Alignment.START, layout);
            add(layout);
        }

        setMargin(true);
        setPadding(false);
        setSizeFull();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        datePicker.setValue(LocalDate.now());
    }

    @Override
    public void valueChanged(ValueChangeEvent<LocalDate> event) {
        LocalDate date = event.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        registration.remove();
        datePicker.setValue(date);
        registration = datePicker.addValueChangeListener(this);

        Rosta rosta = rostaService.buildRosta(date);
        LOGGER.debug("rosta: {}", rosta);

        mapGridBeans(rosta);
        populateGrids();

        String name = String.format("rosta_%s.docx", date.format(FORMATTER));
        buttonWrapper.setResource(new StreamResource(name, () -> {
            InputStream result = null;

            try {
                File tempFile = File.createTempFile("tmp-", ".tmp");
                tempFile.deleteOnExit();

                rostaService.writeRosta(rosta, tempFile);

                result = new FileInputStream(tempFile);
            } catch (IOException e) {
                LOGGER.error("error attempting to write rosta to temporary file", e);
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

    private void mapGridBeans(Rosta rosta) {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RostaDay rostaDay = rosta.getRostaDay(dayOfWeek);

            List<GridBean> gridBeans = List.of(new GridBean(), new GridBean(), new GridBean(), new GridBean());

            for (PartOfDay partOfDay : PartOfDay.values()) {
                User[] users = rostaService.getUsers(rostaDay, partOfDay);

                for (int i = 0; i < Math.min(4, users.length); i++) {
                    gridBeans.get(i).set(partOfDay, users[i].getName());
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
        hl.setWidth("98%");

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

        grid.addColumn(GridBean::getOpener).setHeader(getCentredLabel("Opener")).setWidth("20%").setFlexGrow(0);
        grid.addColumn(GridBean::getMorning).setHeader(getCentredLabel("Morning")).setWidth("40%").setFlexGrow(0);
        grid.addColumn(GridBean::getAfternoon).setHeader(getCentredLabel("Afternoon")).setWidth("40%").setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setSortable(false));

        hl.setVerticalComponentAlignment(Alignment.STRETCH, dayGrid, grid);
        hl.add(dayGrid, grid);

        return hl;
    }

}
