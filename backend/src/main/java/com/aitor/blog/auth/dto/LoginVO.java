package com.aitor.blog.auth.dto;

import lombok.Data;

@Data 
public class LoginVO {
    private String token;
    private String username;

    public LoginVO(String token, String username) {
        this.token = token;
        this.username = username;
    }
}
