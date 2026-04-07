package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    private Authentication getAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не найден");
        }
        return auth;
    }

    public String getCurrentUsername() {
        return getAuth().getName();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    //Возвращает роль из токена
    public String getCurrentUserRole() {
        Authentication auth = getAuth();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");
    }

    //Возвращает объект User из БД
    public User getCurrentUser() {
        return userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }
    }