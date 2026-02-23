package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.QuestionVote;
import com.quyen.qasystem.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionVoteRepository extends JpaRepository<QuestionVote, Long> {

    Optional<QuestionVote> findByUserAndQuestion(User user, Question question);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT qv
    FROM QuestionVote qv
    WHERE qv.user = :user
      AND qv.question = :question
""")
    Optional<QuestionVote> findByUserAndQuestionForUpdate(
            User user,
            Question question
    );
}
