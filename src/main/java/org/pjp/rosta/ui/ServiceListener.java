package org.pjp.rosta.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

@Component
public class ServiceListener implements VaadinServiceInitListener {

    private static final long serialVersionUID = 1110567128557573000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(VaadinServiceInitListener.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addSessionInitListener(initEvent -> {
            LOGGER.debug("A new Session has been initialized!");
        });

        event.getSource().addUIInitListener(initEvent -> {
            LOGGER.debug("A new UI  has been initialized!");
        });
    }
}