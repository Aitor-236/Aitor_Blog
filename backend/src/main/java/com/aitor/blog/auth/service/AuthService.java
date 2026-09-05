package com.aitor.blog.auth.service;

import com.aitor.blog.auth.dto.LoginDTO;
import com.aitor.blog.common.result.Result;

public interface AuthService {
    Result<?> login(LoginDTO loginDTO);
}
