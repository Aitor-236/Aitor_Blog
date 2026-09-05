package com.aitor.blog.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aitor.blog.auth.dto.LoginDTO;
import com.aitor.blog.auth.dto.LoginVO;
import com.aitor.blog.auth.entity.SysUser;
import com.aitor.blog.auth.service.AuthService;
import com.aitor.blog.auth.mapper.SysUserMapper;
import com.aitor.blog.common.result.Result;
import com.aitor.blog.common.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Override
    public Result<?> login(LoginDTO loginDTO) {
        // get username, email, and password  from loginDTO
        String username = loginDTO.getUsername();
        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();

        // Query user by username or email
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.eq(SysUser::getUsername, username);
        } else if (email != null && !email.trim().isEmpty()) {
            queryWrapper.eq(SysUser::getEmail, email);
        } else {
            return Result.error(400, "Username or email must be provided");
        }  
        
        // Fetch user from database
        SysUser user = sysUserMapper.selectOne(queryWrapper);

        // check password
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(500, "Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // Return VO with token and user info
        LoginVO loginVO = new LoginVO(token, user.getUsername(), user.getEmail());
        return Result.success(loginVO);
    }
    
}
