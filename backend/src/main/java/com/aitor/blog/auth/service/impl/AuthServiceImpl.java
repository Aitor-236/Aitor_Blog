package com.aitor.blog.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aitor.blog.auth.dto.LoginVO;
import com.aitor.blog.auth.service.AuthService;
import com.aitor.blog.auth.mapper.SysUserMapper;
import com.aitor.blog.common.result.Result;

public class AuthServiceImpl implements AuthService {

    @Autowired 
    private SysUserMapper sysUserMapper;

    @Autowired 
    private PasswordEncoder passwordEncoder;

    @Override
    public Result<?> login(LoginVO loginVO) {
        // TODO Auto-generated method stub
        return null; 
    }
    
}
