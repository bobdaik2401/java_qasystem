package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.Comment;
import com.quyen.qasystem.entity.CommentVote;
import com.quyen.qasystem.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentVoteRepository
        extends JpaRepository<CommentVote, Long> {

    Optional<CommentVote> findByUserAndComment(User user, Comment comment);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select cv
        from CommentVote cv
        where cv.user = :user and cv.comment = :comment
    """)
    Optional<CommentVote> findByUserAndCommentForUpdate(
            @Param("user") User user,
            @Param("comment") Comment comment
    );
}

