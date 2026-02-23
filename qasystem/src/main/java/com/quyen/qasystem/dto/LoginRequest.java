package com.quyen.qasystem.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginRequest {
    private String usernameOrEmail;// email hoặc username
    private String password;

}
