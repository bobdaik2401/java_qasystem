package com.quyen.qasystem.service;

import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.Role;
import com.quyen.qasystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ================= BASIC =================

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ================= CREATE USER =================

    public User createUser(User user) {

        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // default khi tạo user thường
        user.setRole(Role.USER);
        user.setStatus("ACTIVE");
        user.setEnabled(true);
        user.setVerified(true);

        return userRepository.save(user);
    }

    // ================= ADMIN ACTIONS =================

    /**
     * Xem toàn bộ user (ADMIN)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Chuyển USER -> ADMIN
     */
    @Transactional
    public void makeAdmin(Long userId) {
        User user = getUserById(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("User is already ADMIN");
        }

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    /**
     * Khóa user
     */
    @Transactional
    public void lockUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus("LOCKED");
        userRepository.save(user);
    }

    /**
     * Mở khóa user
     */
    @Transactional
    public void unlockUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }
}

