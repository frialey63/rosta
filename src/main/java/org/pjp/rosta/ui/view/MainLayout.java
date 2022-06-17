package org.pjp.rosta.ui.view;


import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.ui.event.DrawerToggleEvent;
import org.pjp.rosta.ui.view.about.AboutView;
import org.pjp.rosta.ui.view.calendar.CalendarView;
import org.pjp.rosta.ui.view.profile.PasswordChangeView;
import org.pjp.rosta.ui.view.profile.ProfileView;
import org.pjp.rosta.ui.view.rosta.ShopRotaView;
import org.pjp.rosta.ui.view.user.UserManagementView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private static final long serialVersionUID = -4592599326178454171L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainLayout.class);

    /**
     * A simple navigation item component, based on ListItem element.
     */
    static class MenuItemInfo extends ListItem {
        private static final long serialVersionUID = 1369566228598215516L;

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            link.addClassNames("menu-item-link");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            text.addClassNames("menu-item-text");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            private static final long serialVersionUID = 7952058726342246260L;

            public LineAwesomeIcon(String lineawesomeClassnames) {
                addClassNames("menu-item-icon");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }
    }

    private H1 viewTitle;

    private SecurityUtil securityUtil;

    public MainLayout(@Autowired SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames("view-toggle");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");
        toggle.addClickListener(l -> {
            ComponentUtil.fireEvent(UI.getCurrent(), new DrawerToggleEvent(toggle, false));
        });

        viewTitle = new H1();
        viewTitle.addClassNames("view-title");

        HorizontalLayout header;

        if (securityUtil.getAuthenticatedUser() != null) {
            Span filler = new Span();

            Button logout = new Button("Logout", click -> securityUtil.logout());
            header = new CompactHorizontalLayout(toggle, viewTitle, filler, logout);
            header.setFlexGrow(1, filler);
        } else {
            header = new CompactHorizontalLayout(toggle, viewTitle);
        }

        header.addClassNames("view-header");
        header.setPadding(true);

        return header;
    }

    private Component createDrawerContent() {
        H2 appName = new H2("Museum Shop");
        appName.addClassNames("app-name");

        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName, createNavigation(), createFooter());
        section.addClassNames("drawer-section");
        return section;
    }

    private Nav createNavigation() {
        Nav nav = new Nav();
        nav.addClassNames("menu-item-container");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("navigation-list");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);

        }
        return nav;
    }

    private MenuItemInfo[] createMenuItems() {
        boolean admin = securityUtil.getAuthenticatedUser().getAuthorities().stream().filter(ga -> "ROLE_ADMIN".equals(ga.getAuthority())).findFirst().isPresent();

        LOGGER.debug("creating menu items with admin = {}", admin);

        if (admin) {
            return new MenuItemInfo[] {
                    new MenuItemInfo("Rota", "la la-globe", ShopRotaView.class),	// TODO remove for admin but need to dynamically set default route
                    new MenuItemInfo("User Mgmt", "la la-globe", UserManagementView.class),
                    new MenuItemInfo("Password", "la la-globe", PasswordChangeView.class),
                    new MenuItemInfo("About", "la la-file", AboutView.class),
            };
        }

        return new MenuItemInfo[] {
                new MenuItemInfo("Rota", "la la-globe", ShopRotaView.class),
                new MenuItemInfo("Calendar", "la la-globe", CalendarView.class),
                new MenuItemInfo("Profile", "la la-globe", ProfileView.class),
                new MenuItemInfo("Password", "la la-globe", PasswordChangeView.class),
                new MenuItemInfo("About", "la la-file", AboutView.class),
        };
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("footer");

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        Component content = getContent();

        if (content instanceof HasDynamicTitle hdt) {
            return hdt.getPageTitle();
        }

        PageTitle title = content.getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
