package org.pjp.rosta;

import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@EnableMongoRepositories(basePackages = "org.pjp.rosta.repository")
@EnableScheduling
@SpringBootApplication
@Theme(value = "ocimport")
@PWA(name = "RAF Manston History Museum Shop Rota", shortName = "Rota", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class RostaApplication extends SpringBootServletInitializer implements AppShellConfigurator, ApplicationRunner {

    private static final long serialVersionUID = 4107623244717405998L;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public static void main(String[] args) {
        SpringApplication.run(RostaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if ("docker".equals(activeProfile)) {
            rostaService.testData();
            return;
        }

        userService.initData();
    }

    @Scheduled(cron = "${check.rosta.cron}")
    public void checkRosta() {
        rostaService.checkRosta();
    }
}
