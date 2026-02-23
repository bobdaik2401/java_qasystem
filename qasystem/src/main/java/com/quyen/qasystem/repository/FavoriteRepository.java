package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.Favorite;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndQuestion(User user, Question question);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select f from Favorite f
        where f.user = :user and f.question = :question
    """)
    Optional<Favorite> findByUserAndQuestionForUpdate(
            @Param("user") User user,
            @Param("question") Question question
    );

    Page<Favorite> findByUser(User user, Pageable pageable);
}


