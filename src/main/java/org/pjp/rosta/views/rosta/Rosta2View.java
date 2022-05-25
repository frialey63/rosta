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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("The Rosta2")
@Route(value = "rosta2", layout = MainLayout.class)
public class Rosta2View extends VerticalLayout {

    private static final long serialVersionUID = 3386437553156944523L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Rosta2View.class);

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

    public Rosta2View() {
        Label label = new Label("Hello World");

        setHorizontalComponentAlignment(Alignment.START, label);
        add(label);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            HorizontalLayout grid = createGrid(dayOfWeek);
            //grid.setItems(gridBeans);

            setHorizontalComponentAlignment(Alignment.START, grid);
            add(grid);
        }

        setMargin(true);
    }

    private HorizontalLayout createGrid(DayOfWeek dayOfWeek) {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setPadding(false);
        hl.setMargin(false);
        hl.setSpacing(false);
        hl.setWidth("98%");

        Grid<GridBean> grid1 = new Grid<>(GridBean.class, false);
        grid1.setColumnReorderingAllowed(false);
        grid1.setAllRowsVisible(true);
        grid1.setWidth("20%");

        grid1.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        grid1.addThemeVariants(GridVariant.LUMO_COMPACT);

        grid1.addColumn(GridBean::getDay).setHeader(dayOfWeek.getDisplayName(TextStyle.FULL, getLocale())).setWidth("100%").setFlexGrow(0);
        //grid1.addColumn(GridBean::getMorning).setHeader(getCentredLabel("Morning")) .setWidth("40%").setFlexGrow(0);
        //grid1.addColumn(GridBean::getAfternoon).setHeader(getCentredLabel("Afternoon")).setWidth("40%").setFlexGrow(0);

        grid1.getColumns().forEach(c -> c.setSortable(false));

        Grid<GridBean> grid2 = new Grid<>(GridBean.class, false);
        grid2.setColumnReorderingAllowed(false);
        grid2.setAllRowsVisible(true);
        grid2.setWidth("80%");

        grid2.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid2.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid2.addThemeVariants(GridVariant.LUMO_COMPACT);

        //grid2.addColumn(GridBean::getDay).setHeader(dayOfWeek.getDisplayName(TextStyle.FULL, getLocale())).setWidth("20%").setFlexGrow(0);
        grid2.addColumn(GridBean::getMorning).setHeader(getCentredLabel("Morning")).setWidth("50%").setFlexGrow(0);
        grid2.addColumn(GridBean::getAfternoon).setHeader(getCentredLabel("Afternoon")).setWidth("50%").setFlexGrow(0);

        grid2.getColumns().forEach(c -> c.setSortable(false));

        hl.setVerticalComponentAlignment(Alignment.STRETCH, grid1, grid2);
        hl.add(grid1, grid2);

        grid1.setItems(gridBeans);
        grid2.setItems(gridBeans);

        return hl;
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
