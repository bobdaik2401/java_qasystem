package com.quyen.qasystem.dto;

import com.quyen.qasystem.entity.Comment;
import com.quyen.qasystem.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionDetailResponse {

    private Question question;
    private List<Comment> answers;
}
