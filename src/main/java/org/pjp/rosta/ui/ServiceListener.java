package org.pjp.rosta.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vaadin.componentfactory.IdleNotification;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

@Component
public class ServiceListener implements VaadinServiceInitListener {

    private static final long serialVersionUID = 1110567128557573000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(VaadinServiceInitListener.class);

    private static final int SECONDS_BEFORE_NOTIFICATION = 60;

    @Value("${session.max.inactive.interval:60}")
    private int maxInactiveInterval;

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addSessionInitListener(initEvent -> {
            LOGGER.debug("A new Session has been initialized! maxInactiveInterval = {}", maxInactiveInterval);
            initEvent.getSession().getSession().setMaxInactiveInterval(maxInactiveInterval);
        });

        event.getSource().addUIInitListener(initEvent -> {
            LOGGER.debug("A new UI  has been initialized! maxInactiveInterval = {}, secondsBeforeNotification = {}", maxInactiveInterval, SECONDS_BEFORE_NOTIFICATION);

            initEvent.getUI().getPage().fetchCurrentURL(url -> {
                String path = url.getPath();

                if ("/login".equals(path) || "/register".equals(path)) {
                    // skip
                } else {
                    if (maxInactiveInterval > 0) {
                        IdleNotification idleNotification = new IdleNotification(
                                "Your session will expire in " + IdleNotification.MessageFormatting.SECS_TO_TIMEOUT + " seconds.",
                                SECONDS_BEFORE_NOTIFICATION,
                                maxInactiveInterval,
                                "login");
                        idleNotification.addExtendSessionButton("Extend session");
                        idleNotification.addRedirectButton("Logout now", "login");
                        idleNotification.setExtendSessionOnOutsideClick(false);

                        initEvent.getUI().add(idleNotification);
                    }
                }
            });
        });
    }
}