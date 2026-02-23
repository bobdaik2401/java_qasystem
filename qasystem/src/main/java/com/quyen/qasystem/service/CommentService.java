package com.quyen.qasystem.service;

import com.quyen.qasystem.dto.CommentResponse;
import com.quyen.qasystem.entity.Comment;
import com.quyen.qasystem.entity.CommentVote;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.RateLimitAction;
import com.quyen.qasystem.enums.VoteType;
import com.quyen.qasystem.exception.AccessDeniedException;
import com.quyen.qasystem.exception.ResourceNotFoundException;
import com.quyen.qasystem.repository.CommentRepository;
import com.quyen.qasystem.repository.CommentVoteRepository;
import com.quyen.qasystem.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final RateLimitService rateLimitService;
    public CommentService(
            CommentRepository commentRepository,
            QuestionRepository questionRepository,
            CommentVoteRepository commentVoteRepository,
            RateLimitService rateLimitService
    ) {
        this.commentRepository = commentRepository;
        this.questionRepository = questionRepository;
        this.commentVoteRepository = commentVoteRepository;
        this.rateLimitService = rateLimitService;
    }

    /* =====================================================
       1️⃣ LẤY ANSWER CỦA 1 CÂU HỎI
    ===================================================== */
    public Page<CommentResponse> getCommentsByQuestion(
            Long questionId,
            Pageable pageable,
            User user
    ) {
        Question question = questionRepository
                .findByIdAndDeletedFalse(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        Page<Comment> page = commentRepository
                .findByQuestionAndDeletedFalseOrderByAcceptedDescUpvoteDescCreatedAtAsc(
                        question,
                        pageable
                );

        return page.map(comment -> {
            VoteType myVote = commentVoteRepository
                    .findByUserAndComment(user, comment)
                    .map(CommentVote::getType)
                    .orElse(null);

            return new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getUpvote(),
                    comment.getDownvote(),
                    comment.isAccepted(),
                    myVote,
                    comment.getCreatedAt()
            );
        });
    }


    /* =====================================================
       2️⃣ THÊM ANSWER
    ===================================================== */
    public Comment addComment(Long questionId, String content, User user) {

        Question question = questionRepository
                .findByIdAndDeletedFalse(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        Comment comment = Comment.builder()
                .content(content)
                .question(question)
                .user(user)
                .upvote(0)
                .downvote(0)
                .accepted(false)
                .deleted(false)
                .build();

        return commentRepository.save(comment);
    }

    /* =====================================================
       3️⃣ XOÁ ANSWER (SOFT DELETE)
    ===================================================== */
    public void deleteComment(Long commentId, User user) {

        Comment comment = commentRepository
                .findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())
                && !"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("No permission");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    /* =====================================================
       4️⃣ ACCEPT ANSWER
    ===================================================== */
    @Transactional
    public void acceptComment(Long commentId, User user) {

        Comment comment = commentRepository
                .findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getQuestion().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only question owner can accept answer");
        }

        commentRepository.unacceptAllByQuestionId(comment.getQuestion().getId());
        comment.setAccepted(true);
        commentRepository.save(comment);
    }

    @Transactional
    public void voteAnswer(Long commentId, VoteType newType, User user) {

        rateLimitService.check(
                user,
                RateLimitAction.VOTE_COMMENT,
                10,
                60
        );

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // ❌ CHẶN TỰ VOTE
        if (comment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot vote your own answer");
        }

        // 🔒 LOCK vote row của user
        Optional<CommentVote> optionalVote =
                commentVoteRepository.findByUserAndCommentForUpdate(user, comment);

        // ===== CHƯA TỪNG VOTE =====
        if (optionalVote.isEmpty()) {
            createVote(commentId, comment, user, newType);
            return;
        }

        CommentVote existingVote = optionalVote.get();

        // ===== SAME TYPE → UNVOTE =====
        if (existingVote.getType() == newType) {
            removeVote(commentId, existingVote);
            return;
        }

        // ===== SWITCH UP ↔ DOWN =====
        switchVote(commentId, existingVote, newType);
    }

    /* ================= PRIVATE ================= */

    private void createVote(
            Long commentId,
            Comment comment,
            User user,
            VoteType type
    ) {
        if (type == VoteType.UP) {
            commentRepository.increaseUpvote(commentId);
        } else {
            commentRepository.increaseDownvote(commentId);
        }

        commentVoteRepository.save(
                CommentVote.builder()
                        .user(user)
                        .comment(comment)
                        .type(type)
                        .build()
        );
    }

    private void removeVote(Long commentId, CommentVote vote) {
        if (vote.getType() == VoteType.UP) {
            commentRepository.decreaseUpvote(commentId);
        } else {
            commentRepository.decreaseDownvote(commentId);
        }

        commentVoteRepository.delete(vote);
    }

    private void switchVote(
            Long commentId,
            CommentVote vote,
            VoteType newType
    ) {
        if (newType == VoteType.UP) {
            commentRepository.decreaseDownvote(commentId);
            commentRepository.increaseUpvote(commentId);
        } else {
            commentRepository.decreaseUpvote(commentId);
            commentRepository.increaseDownvote(commentId);
        }

        vote.setType(newType);
        commentVoteRepository.save(vote);
    }


}
