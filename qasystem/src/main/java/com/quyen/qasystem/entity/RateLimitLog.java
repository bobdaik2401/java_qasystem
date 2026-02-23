package com.quyen.qasystem.entity;

import com.quyen.qasystem.enums.RateLimitAction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rate_limit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitLog extends BaseCreateEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private RateLimitAction action;

}
