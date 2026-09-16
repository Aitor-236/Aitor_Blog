package com.aitor.blog.article.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aitor.blog.article.dto.ArticleDTO;
import com.aitor.blog.article.dto.ArticleDeleteResult;
import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.service.AdminArticleService;
import com.aitor.blog.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;

/**
 * 后台文章管理接口。controller 只负责参数绑定和 Result 包装，
 * 参数校验和业务异常都在 service 层处理，由 GlobalExceptionHandler 统一返回。
 */
@RestController 
@RequestMapping ("/admin/article")
@RequiredArgsConstructor 
public class AdminArticleController {
    
    private final AdminArticleService adminArticleService;

    /**
     * 管理员的文章卡片分页列表，草稿和已发布都会返回，按最近更新时间倒序。
     */
    @GetMapping("/list")
    public Result<Page<ArticleVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminArticleService.listAll(page, size, category, keyword));
    }

    /**
     * 新建文章，落库为草稿，返回创建后的文章。
     */
    @PostMapping("/create")
    public Result<ArticleVO> create(@RequestBody ArticleDTO articleDTO) {
        return Result.success(adminArticleService.createArticle(articleDTO));
    }

    /**
     * 修改文章：只更新请求体里带了值的字段，分类按名称或英文标识匹配，
     * id 放在请求体里传。
     */
    @PostMapping("/update")
    public Result<ArticleVO> update(@RequestBody ArticleDTO articleDTO) {
        return Result.success(adminArticleService.updateArticle(articleDTO));
    }

    /**
     * 删除文章：已发布只取消发布（返回 UNPUBLISHED，文章回退为草稿），
     * 草稿则物理删除（返回 DELETED）。
     */
    @PostMapping("/delete")
    public Result<ArticleDeleteResult> delete(@RequestParam Long id) {
        return Result.success(adminArticleService.deleteArticle(id));
    }

    /**
     * 发布文章：写入发布时间并置为已发布；已发布的文章重复调用不做任何改动。
     */
    @PostMapping("/publish")
    public Result<ArticleVO> publish(@RequestParam Long id) {
        return Result.success(adminArticleService.publishArticle(id));
    }

    /**
     * 物理删除文章：只允许删草稿，已发布的会返回 400 提示先取消发布。
     */
    @PostMapping("/force-delete")
    public Result<Void> forceDelete(@RequestParam Long id) {
        adminArticleService.forceDeleteArticle(id);
        return Result.success();
    }

}
