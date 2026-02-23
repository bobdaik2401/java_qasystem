package com.quyen.qasystem.service;

import com.quyen.qasystem.dto.QuestionDetailResponse;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.VoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IQuestionService {

    // TAB 1: list + search
    Page<Question> getAllQuestions(String keyword, Pageable pageable);

    // TAB 2: câu hỏi của tôi
    Page<Question> getMyQuestions(User user, Pageable pageable);

    // Question đơn (chỉ entity)
    Question getQuestionById(Long id);

    // Question detail (entity + answers)
    QuestionDetailResponse getQuestionDetail(Long id, Pageable pageable);
    void voteQuestion(Long questionId, VoteType type, User user);
    void toggleFavorite(Long questionId, User user);
}
