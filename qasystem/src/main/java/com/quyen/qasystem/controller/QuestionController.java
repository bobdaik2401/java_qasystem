package com.quyen.qasystem.controller;

import com.quyen.qasystem.dto.QuestionDetailResponse;
import com.quyen.qasystem.dto.VoteRequest;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.Role;
import com.quyen.qasystem.enums.VoteType;
import com.quyen.qasystem.service.IQuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;
    private final IQuestionService favoriteService;

    public QuestionController(IQuestionService questionService, IQuestionService favoriteService) {
        this.questionService = questionService;
        this.favoriteService = favoriteService;
    }

    // 1️⃣ Detail đơn (question only)
    @GetMapping("/{id}")
    public Question getDetail(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    // 2️⃣ Detail đầy đủ (question + answers)
    @GetMapping("/{id}/detail")
    public QuestionDetailResponse getQuestionDetail(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return questionService.getQuestionDetail(id, pageable);
    }

    // TAB 1: list + search
    @GetMapping
    public Page<Question> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return questionService.getAllQuestions(keyword, pageable);
    }

    // TAB 2: câu hỏi của tôi
    @GetMapping("/me")
    public Page<Question> myQuestions(Pageable pageable) {
        User user = mockCurrentUser();
        return questionService.getMyQuestions(user, pageable);
    }

    // MOCK USER
    private User mockCurrentUser() {
        return User.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();
    }
    @PutMapping("/{id}/vote")
    public void voteQuestion(
            @PathVariable Long id,
            @RequestBody VoteRequest request
    ) {
        User user = mockCurrentUser();
        questionService.voteQuestion(id, VoteType.valueOf(request.getType()), user);
    }
    @PutMapping("{id}/favorite")
    public void toggleFavorite(@PathVariable Long id) {
        User user = mockCurrentUser();
        favoriteService.toggleFavorite(id, user);
    }

}
