package com.aitor.blog.auth.service;

import com.aitor.blog.auth.dto.LoginDTO;
import com.aitor.blog.auth.dto.LoginVO;

public interface AuthService {

    /** 校验账号密码并签发 token，失败时抛业务异常。 */
    LoginVO login(LoginDTO loginDTO);
}
