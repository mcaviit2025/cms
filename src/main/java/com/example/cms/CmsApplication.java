package com.example.cms;

import com.example.cms.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CmsApplication implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin on startup
        adminService.createDefaultAdmin();
    }
}