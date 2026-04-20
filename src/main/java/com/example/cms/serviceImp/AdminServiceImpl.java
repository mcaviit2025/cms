package com.example.cms.serviceImp;

import com.example.cms.controller.dto.LoginRequest;
import com.example.cms.entity.Admin;
import com.example.cms.repository.AdminRepository;
import com.example.cms.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public void createDefaultAdmin() {
        if (!adminRepository.existsByEmail("admin@cms.com")) {
            Admin admin = new Admin();
            admin.setEmail("admin@cms.com");
            admin.setPassword("admin123");  // Plain text password
            admin.setName("Super Admin");
            adminRepository.save(admin);
            System.out.println("========================================");
            System.out.println("Default admin created:");
            System.out.println("Email: admin@cms.com");
            System.out.println("Password: admin123");
            System.out.println("========================================");
        }
    }

    @Override
    public boolean authenticate(LoginRequest loginRequest) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(loginRequest.getEmail());

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            // Plain text password comparison
            return admin.getPassword().equals(loginRequest.getPassword());
        }
        return false;
    }

    @Override
    public Admin findByEmail(String email) {
        return null;
    }
}