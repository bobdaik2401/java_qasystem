package com.quyen.qasystem.entity;

import com.quyen.qasystem.enums.VoteType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "question_votes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "question_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionVote extends BaseCreateEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType type;

}
