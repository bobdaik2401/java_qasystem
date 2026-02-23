package com.quyen.qasystem.dto;

import com.quyen.qasystem.enums.VoteType;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        int upvote,
        int downvote,
        boolean accepted,
        VoteType myVote,
        LocalDateTime createdAt
) {}
