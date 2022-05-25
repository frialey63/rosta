package org.pjp.rosta.views.rosta;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;

import org.pjp.rosta.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("The Rosta")
@Route(value = "rosta", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class RostaView extends VerticalLayout {

    private static final long serialVersionUID = 3386437553156944523L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaView.class);

    public static class GridBean {
        private String day;
        private String morning;
        private String afternoon;

        public GridBean(String morning, String afternoon) {
            super();
            this.day = null;
            this.morning = morning;
            this.afternoon = afternoon;
        }

        public String getDay() {
            return day;
        }

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

    }

    private List<GridBean> gridBeans = new ArrayList<>();

    {
        gridBeans.add(new GridBean("Fred", "Bill"));
        gridBeans.add(new GridBean("Fred", "Bill"));
        gridBeans.add(new GridBean("Fred", "Bill"));
        gridBeans.add(new GridBean("Fred", "Bill"));
    }

    public RostaView() {
        Label label = new Label("Hello World");

        setHorizontalComponentAlignment(Alignment.START, label);
        add(label);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            Grid<GridBean> grid = createGrid(dayOfWeek);
            grid.setItems(gridBeans);

            setHorizontalComponentAlignment(Alignment.START, grid);
            add(grid);
        }

        setMargin(true);
    }

    private Grid<GridBean> createGrid(DayOfWeek dayOfWeek) {
        Grid<GridBean> grid = new Grid<>(GridBean.class, false);
        grid.setColumnReorderingAllowed(false);
        grid.setAllRowsVisible(true);
        grid.setWidth("98%");

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        grid.addColumn(GridBean::getDay).setHeader(dayOfWeek.getDisplayName(TextStyle.FULL, getLocale())).setWidth("20%").setFlexGrow(0);
        grid.addColumn(GridBean::getMorning).setHeader(getCentredLabel("Morning")) .setWidth("40%").setFlexGrow(0);
        grid.addColumn(GridBean::getAfternoon).setHeader(getCentredLabel("Afternoon")).setWidth("40%").setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setSortable(false));

        return grid;
    }

    private VerticalLayout getCentredLabel(String text) {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.setMargin(false);
        Label label = new Label(text);
        vl.setHorizontalComponentAlignment(Alignment.CENTER, label);
        vl.add(label);
        return vl;
    }

}
