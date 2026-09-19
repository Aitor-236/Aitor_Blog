package com.aitor.blog.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitor.blog.auth.dto.SiteOwnerVO;
import com.aitor.blog.auth.service.SiteProfileService;
import com.aitor.blog.common.result.Result;

import lombok.RequiredArgsConstructor;

/**
 * 前台站点资料接口，匿名可访问（路径已加进 JwtInterceptor 白名单）。
 */
@RestController
@RequestMapping("/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteProfileService siteProfileService;

    /** 首页要展示的站长资料：角色最高的那个账号的用户名和头像。 */
    @GetMapping("/owner")
    public Result<SiteOwnerVO> owner() {
        return Result.success(siteProfileService.getSiteOwner());
    }
}
