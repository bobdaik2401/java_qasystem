package com.quyen.qasystem.controller;

import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.Role;
import com.quyen.qasystem.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    private User mockCurrentUser() {
        return User.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();
    }
    @PutMapping("/questions/{id}")
    public void toggleFavorite(@PathVariable Long id) {
        User user = mockCurrentUser();
        favoriteService.toggleFavorite(id, user);
    }

    @GetMapping
    public Page<Question> myFavorites(Pageable pageable) {
        User user = mockCurrentUser();
        return favoriteService.getFavoriteQuestions(user, pageable);
    }
}

