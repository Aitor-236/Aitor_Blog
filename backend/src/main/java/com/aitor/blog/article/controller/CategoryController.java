package com.aitor.blog.article.controller;

import java.util.List;

import com.aitor.blog.article.dto.CategoryVO;
import com.aitor.blog.article.service.CategoryService;
import com.aitor.blog.common.result.Result;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 公开的分类列表，附已发布文章数，供文章页右侧面板使用。
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.listWithArticleCounts());
    }
}
