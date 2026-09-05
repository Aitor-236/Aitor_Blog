package com.aitor.blog.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aitor.blog.auth.dto.LoginDTO;
import com.aitor.blog.auth.service.AuthService;
import com.aitor.blog.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }
    
}
