package org.pjp.rosta;

import java.time.LocalDate;

import org.pjp.rosta.bean.Rosta;
import org.pjp.rosta.service.RostaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "org.pjp.rosta.repository")
@SpringBootApplication
public class RostaApplication implements ApplicationRunner {

    @Autowired
    private RostaService service;

    public static void main(String[] args) {
        SpringApplication.run(RostaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        service.initData();

        Rosta rosta = service.buildRosta(LocalDate.of(2022, 5, 18));

        System.out.println(rosta);
    }

}
