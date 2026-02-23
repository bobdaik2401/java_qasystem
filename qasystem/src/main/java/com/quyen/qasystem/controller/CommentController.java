package com.quyen.qasystem.controller;

import com.quyen.qasystem.dto.CommentRequest;
import com.quyen.qasystem.dto.CommentResponse;
import com.quyen.qasystem.dto.VoteRequest;
import com.quyen.qasystem.entity.Comment;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.Role;
import com.quyen.qasystem.enums.VoteType;
import com.quyen.qasystem.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 1️⃣ Lấy answer của 1 câu hỏi
    @GetMapping("/question/{questionId}")
    public Page<CommentResponse> getCommentsByQuestion(
            @PathVariable Long questionId,
            Pageable pageable
    ) {
        User user = mockCurrentUser();
        return commentService.getCommentsByQuestion(questionId, pageable, user);
    }

    // 2️⃣ Trả lời câu hỏi
    @PostMapping("/question/{questionId}")
    public Comment addComment(
            @PathVariable Long questionId,
            @RequestBody CommentRequest request
    ) {
        User user = mockCurrentUser();
        return commentService.addComment(questionId, request.content(), user);
    }


    // 3️⃣ Xóa answer
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        User user = mockCurrentUser();
        commentService.deleteComment(id, user);
    }

    // 4️⃣ Accept answer
    @PutMapping("/{id}/accept")
    public void acceptComment(@PathVariable Long id) {
        User user = mockCurrentUser();
        commentService.acceptComment(id, user);
    }

    private User mockCurrentUser() {
        return User.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();
    }
    // 5️⃣ Vote answer (NÂNG CAO)
    @PutMapping(value = "/{id}/vote", consumes = "application/json")
    public void vote(
            @PathVariable Long id,
            @RequestBody VoteRequest request
    ) {
        User user = mockCurrentUser();
        commentService.voteAnswer(
                id,
                VoteType.valueOf(request.getType()),
                user
        );
    }

}
