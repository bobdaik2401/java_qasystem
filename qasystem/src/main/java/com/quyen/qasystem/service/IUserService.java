package com.quyen.qasystem.service;

import com.quyen.qasystem.entity.User;

import java.util.Optional;

public interface IUserService {

    User getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    User createUser(User user);

    void lockUser(Long userId);

    void unlockUser(Long userId);
}
