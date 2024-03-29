package org.pjp.rosta;

import org.apache.commons.compress.utils.Sets;
import org.pjp.rosta.service.DocumentService;
import org.pjp.rosta.service.RotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Push
@Theme(value = "ocimport")
@PWA(name = "RAF Manston History Museum Shop Rota", shortName = "Rota", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@EnableScheduling
@SpringBootApplication
public class RostaApplication extends SpringBootServletInitializer implements AppShellConfigurator, ApplicationRunner {

    private static final long serialVersionUID = 4107623244717405998L;

    @Value("${spring.profiles.active:default}")
    private String[] springProfilesActive;

    @Autowired
    private RotaService rotaService;

    @Autowired
    private DocumentService documentService;

    public static void main(String[] args) {
        SpringApplication.run(RostaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Sets.newHashSet(springProfilesActive).contains("test")) {
            rotaService.testData();
            documentService.testData();
            return;
        }

        rotaService.sendTestEmail();
    }

    @Scheduled(cron = "${check.rota.cron}")
    public void checkRosta() {
        rotaService.checkRotaAndNotify();
    }
}
