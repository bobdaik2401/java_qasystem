package com.quyen.qasystem.service;

import com.quyen.qasystem.entity.Favorite;
import com.quyen.qasystem.entity.Question;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.AuditAction;
import com.quyen.qasystem.exception.ResourceNotFoundException;
import com.quyen.qasystem.repository.FavoriteRepository;
import com.quyen.qasystem.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final QuestionRepository questionRepository;
    private final AuditService auditService;

    @Transactional
    public void toggleFavorite(Long questionId, User user) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // ❌ Không cho tự favorite câu hỏi của mình
        if (question.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot favorite your own question");
        }

        Optional<Favorite> optionalFavorite =
                favoriteRepository.findByUserAndQuestionForUpdate(user, question);

        // ⭐ ADD FAVORITE
        if (optionalFavorite.isEmpty()) {

            favoriteRepository.save(
                    Favorite.builder()
                            .user(user)
                            .question(question)
                            .build()
            );

            questionRepository.increaseFavorite(questionId);

            auditService.log(
                    user,
                    AuditAction.ADD_FAVORITE,
                    "QUESTION",
                    questionId,
                    "User added favorite"
            );
            return;
        }

        // ❌ REMOVE FAVORITE
        favoriteRepository.delete(optionalFavorite.get());
        questionRepository.decreaseFavorite(questionId);

        auditService.log(
                user,
                AuditAction.REMOVE_FAVORITE,
                "QUESTION",
                questionId,
                "User removed favorite"
        );
    }

    // 📄 LIST FAVORITES
    public Page<Question> getFavoriteQuestions(User user, Pageable pageable) {
        return favoriteRepository.findByUser(user, pageable)
                .map(Favorite::getQuestion);
    }
}
