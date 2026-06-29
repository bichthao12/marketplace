package com.marketplace.user.service;

import com.marketplace.auth.exception.EmailAlreadyExistsException;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User createUser(String email, String rawPassword, String fullName, String phone) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName.trim());
        user.setPhone(phone);
        user.setRoles(Set.of(Role.BUYER));
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(UUID userId, String rawPassword) {
        User user = getById(userId);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = getById(userId);
        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public void addRole(UUID userId, Role role) {
        User user = getById(userId);
        user.addRole(role);
        userRepository.save(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
