package com.quyen.qasystem.service;

import com.quyen.qasystem.entity.RateLimitLog;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.RateLimitAction;
import com.quyen.qasystem.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitRepository rateLimitRepository;

    public void check(User user, RateLimitAction action, int limit, int seconds) {

        LocalDateTime from = LocalDateTime.now().minusSeconds(seconds);

        long count = rateLimitRepository.countRecent(user, action, from);

        if (count >= limit) {
            throw new IllegalStateException(
                    "Too many actions. Please slow down."
            );
        }
    }

    public void log(User user, RateLimitAction action) {
        rateLimitRepository.save(
                RateLimitLog.builder()
                        .user(user)
                        .action(action)
                        .build()
        );
    }
}
