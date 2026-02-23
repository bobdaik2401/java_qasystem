package com.quyen.qasystem.repository;


import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface QuestionRepository extends JpaRepository<Question, Long>{
    // TAB 1: list toàn công ty + pagination
    Page<Question> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // TAB 1: search + pagination
    @Query("""
        SELECT q
        FROM Question q
        WHERE q.deleted = false
          AND LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY q.createdAt DESC
    """)
    Page<Question> searchQuestions(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // TAB 2: câu hỏi của tôi
    Page<Question> findByUserAndDeletedFalseOrderByCreatedAtDesc(
            User user,
            Pageable pageable
    );
    Optional<Question> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("update Question q set q.upvote = q.upvote + 1 where q.id = :id")
    void increaseUpvote(@Param("id") Long id);

    @Modifying
    @Query("update Question q set q.upvote = q.upvote - 1 where q.id = :id and q.upvote > 0")
    void decreaseUpvote(@Param("id") Long id);

    @Modifying
    @Query("update Question q set q.downvote = q.downvote + 1 where q.id = :id")
    void increaseDownvote(@Param("id") Long id);

    @Modifying
    @Query("update Question q set q.downvote = q.downvote - 1 where q.id = :id and q.downvote > 0")
    void decreaseDownvote(@Param("id") Long id);

    @Modifying
    @Query("update Question q set q.favoriteCount = q.favoriteCount + 1 where q.id = :id")
    void increaseFavorite(@Param("id") Long id);

    @Modifying
    @Query("update Question q set q.favoriteCount = q.favoriteCount - 1 where q.id = :id")
    void decreaseFavorite(@Param("id") Long id);

}
