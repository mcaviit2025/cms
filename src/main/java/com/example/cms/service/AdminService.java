package com.example.cms.service;

import com.example.cms.controller.dto.LoginRequest;
import com.example.cms.entity.Admin;

public interface AdminService {
    void createDefaultAdmin();
    boolean authenticate(LoginRequest loginRequest);
    Admin findByEmail(String email);
}