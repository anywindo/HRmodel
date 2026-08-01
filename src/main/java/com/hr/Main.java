package com.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import ui.ManageEmployee;

@SpringBootApplication(scanBasePackages = {"com.hr", "ui", "model"})
@EnableAdminServer
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ManageEmployee manageEmployee = new ManageEmployee();
        manageEmployee.showMenu();
    }
}
