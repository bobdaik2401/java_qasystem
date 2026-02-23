package com.quyen.qasystem.service;

import com.quyen.qasystem.dto.QuestionDetailResponse;
import com.quyen.qasystem.entity.*;
import com.quyen.qasystem.enums.AuditAction;
import com.quyen.qasystem.enums.RateLimitAction;
import com.quyen.qasystem.enums.VoteType;
import com.quyen.qasystem.exception.ResourceNotFoundException;
import com.quyen.qasystem.repository.CommentRepository;
import com.quyen.qasystem.repository.FavoriteRepository;
import com.quyen.qasystem.repository.QuestionRepository;
import com.quyen.qasystem.repository.QuestionVoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionService implements IQuestionService {

    private final QuestionRepository questionRepository;
    private final CommentRepository commentRepository;
    private final QuestionVoteRepository questionVoteRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    public QuestionService(
            QuestionRepository questionRepository,
            CommentRepository commentRepository,
            QuestionVoteRepository questionVoteRepository,
            FavoriteRepository favoriteRepository,
            AuditService auditService,
            RateLimitService rateLimitService
    ) {
        this.questionRepository = questionRepository;
        this.commentRepository = commentRepository;
        this.questionVoteRepository = questionVoteRepository;
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;
        this.rateLimitService = rateLimitService;
    }

    // TAB 1: list + search
    @Override
    public Page<Question> getAllQuestions(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return questionRepository
                    .findByDeletedFalseOrderByCreatedAtDesc(pageable);
        }
        return questionRepository.searchQuestions(keyword, pageable);
    }

    // TAB 2: câu hỏi của tôi
    @Override
    public Page<Question> getMyQuestions(User user, Pageable pageable) {
        return questionRepository
                .findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable);
    }

    // Question đơn
    @Override
    public Question getQuestionById(Long id) {
        return questionRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    // Question detail (question + answers)
    @Override
    public QuestionDetailResponse getQuestionDetail(Long questionId, Pageable pageable) {

        Question question = questionRepository
                .findByIdAndDeletedFalse(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        Page<Comment> answers =
                commentRepository
                        .findByQuestionAndDeletedFalseOrderByAcceptedDescUpvoteDescCreatedAtAsc(
                                question,
                                pageable
                        );

        return new QuestionDetailResponse(question, answers.getContent());
    }

    @Transactional
    public void voteQuestion(Long questionId, VoteType newType, User user) {

        rateLimitService.check(
                user,
                RateLimitAction.VOTE_QUESTION,
                10,
                60
        );

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // ❌ CHẶN TỰ VOTE
        if (question.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot vote your own question");
        }

        Optional<QuestionVote> optionalVote =
                questionVoteRepository.findByUserAndQuestionForUpdate(user, question);

        // 1️⃣ CHƯA VOTE
        if (optionalVote.isEmpty()) {
            createVote(questionId, question, user, newType);
            auditService.log(
                    user,
                    AuditAction.VOTE_QUESTION,
                    "QUESTION",
                    questionId,
                    "User voted question"
            );
            return;
        }

        QuestionVote existingVote = optionalVote.get();

        // 2️⃣ SAME TYPE → UNVOTE
        if (existingVote.getType() == newType) {
            removeVote(questionId, existingVote);
            auditService.log(
                    user,
                    AuditAction.VOTE_QUESTION,
                    "QUESTION",
                    questionId,
                    "User unvoted question"
            );
            return;
        }

        // 3️⃣ SWITCH UP ↔ DOWN
        switchVote(questionId, existingVote, newType);
        auditService.log(
                user,
                AuditAction.VOTE_QUESTION,
                "QUESTION",
                questionId,
                "User switched vote"
        );
    }

    private void createVote(
            Long questionId,
            Question question,
            User user,
            VoteType type
    ) {
        if (type == VoteType.UP) {
            questionRepository.increaseUpvote(questionId);
        } else {
            questionRepository.increaseDownvote(questionId);
        }

        questionVoteRepository.save(
                QuestionVote.builder()
                        .user(user)
                        .question(question)
                        .type(type)
                        .build()
        );
    }


    private void removeVote(Long questionId, QuestionVote vote) {

        if (vote.getType() == VoteType.UP) {
            questionRepository.decreaseUpvote(questionId);
        } else {
            questionRepository.decreaseDownvote(questionId);
        }

        questionVoteRepository.delete(vote);
    }

    private void switchVote(
            Long questionId,
            QuestionVote vote,
            VoteType newType
    ) {
        if (newType == VoteType.UP) {
            questionRepository.decreaseDownvote(questionId);
            questionRepository.increaseUpvote(questionId);
        } else {
            questionRepository.decreaseUpvote(questionId);
            questionRepository.increaseDownvote(questionId);
        }

        vote.setType(newType);
    }
    @Transactional
    public void toggleFavorite(Long questionId, User user) {

        rateLimitService.check(
                user,
                RateLimitAction.FAVORITE,
                5,
                60
        );
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // ❌ không cho favorite câu hỏi của chính mình
        if (question.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot favorite your own question");
        }

        Optional<Favorite> optionalFavorite =
                favoriteRepository.findByUserAndQuestionForUpdate(user, question);

        // ➕ CHƯA FAVORITE → ADD
        if (optionalFavorite.isEmpty()) {
            favoriteRepository.save(
                    Favorite.builder()
                            .user(user)
                            .question(question)
                            .build()
            );
            auditService.log(
                    user,
                    AuditAction.ADD_FAVORITE,
                    "QUESTION",
                    questionId,
                    "User added question to favorites"
            );
            return;
        }

        // ❌ ĐÃ FAVORITE → REMOVE (toggle)
        favoriteRepository.delete(optionalFavorite.get());
    }

}
