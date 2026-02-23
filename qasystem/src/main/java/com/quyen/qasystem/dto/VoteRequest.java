package com.quyen.qasystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private String type; // "UP" | "DOWN"
}
