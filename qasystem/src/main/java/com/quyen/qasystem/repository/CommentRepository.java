package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.Comment;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 1️⃣ Lấy toàn bộ answer của 1 câu hỏi (forum-style)
    Page<Comment> findByQuestionAndDeletedFalseOrderByAcceptedDescUpvoteDescCreatedAtAsc(
            Question question,
            Pageable pageable
    );

    @Modifying

    @Query("""
    UPDATE Comment c
    SET c.accepted = false
    WHERE c.question.id = :questionId
""")
    void unacceptAllByQuestionId(@Param("questionId")Long questionId);

    // 2️⃣ Lấy answer của user (profile / quản lý cá nhân)
    Page<Comment> findByUserAndDeletedFalseOrderByCreatedAtDesc(
            User user,
            Pageable pageable
    );

    Optional<Comment> findByIdAndDeletedFalse(Long id);

    // 3️⃣ Dùng để kiểm tra quyền (xóa / sửa)
    boolean existsByIdAndUser(Long id, User user);

    //Tránh spam
    @Modifying
    @Query("UPDATE Comment c SET c.upvote = c.upvote + 1 WHERE c.id = :id")
    void increaseUpvote(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.upvote = c.upvote - 1 WHERE c.id = :id")
    void decreaseUpvote(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.downvote = c.downvote + 1 WHERE c.id = :id")
    void increaseDownvote(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.downvote = c.downvote - 1 WHERE c.id = :id")
    void decreaseDownvote(@Param("id") Long id);

}
