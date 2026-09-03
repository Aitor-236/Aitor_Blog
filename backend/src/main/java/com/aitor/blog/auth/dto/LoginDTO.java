package com.aitor.blog.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;


@Data 
public class LoginDTO {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
