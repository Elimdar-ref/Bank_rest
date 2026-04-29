package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    void register(RegisterRequest request);
}