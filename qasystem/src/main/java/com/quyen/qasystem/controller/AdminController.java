package com.quyen.qasystem.controller;

import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    // Constructor injection (CHUẨN)
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy thông tin user theo ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    /**
     * Khóa user (ADMIN)
     * PUT /api/users/{id}/lock
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<String> lockUser(@PathVariable Long id) {
        userService.lockUser(id);
        return ResponseEntity.ok("User locked successfully");
    }

    /**
     * Mở khóa user (ADMIN)
     * PUT /api/users/{id}/unlock
     */
    @PutMapping("/{id}/unlock")
    public ResponseEntity<String> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ResponseEntity.ok("User unlocked successfully");
    }

    //role user->admin
    @PutMapping("/users/{id}/make-admin")
    public void makeAdmin(@PathVariable Long id) {
        userService.makeAdmin(id);
    }

}
