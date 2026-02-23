package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.RateLimitLog;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.RateLimitAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RateLimitRepository extends JpaRepository<RateLimitLog, Long> {

    @Query("""
        select count(r)
        from RateLimitLog r
        where r.user = :user
          and r.action = :action
          and r.createdAt >= :from
    """)
    long countRecent(
            @Param("user") User user,
            @Param("action") RateLimitAction action,
            @Param("from") LocalDateTime from
    );
}
