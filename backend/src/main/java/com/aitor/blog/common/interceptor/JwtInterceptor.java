package com.aitor.blog.common.interceptor;

import com.aitor.blog.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {
    @Override 
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        // Allow preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; 
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            if (JwtUtil.verifyToken(token)) {
                return true; // Token is valid, proceed with the request
            }
        }
        // Token is missing or invalid, request 401
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401, \"message\": \"Unauthorized: Invalid or missing token.\", \"data\": null }");
        return false;
    }
}
