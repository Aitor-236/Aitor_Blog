package com.aitor.blog.article.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.aitor.blog.article.mapper.ArticleMapper;
import com.aitor.blog.article.service.ArticleService;
import com.aitor.blog.common.utils.PageParam;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String STATUS_PUBLISHED = "published";

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper articleCategoryMapper;
    private final ArticleVOAssembler articleVOAssembler;

    @Override
    public Page<ArticleVO> listPublished(long page, long size, String categorySlug, String keyword) {
        size = PageParam.requireValid(page, size);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, STATUS_PUBLISHED)
                .orderByDesc(Article::getPublishedAt);

        if (StringUtils.hasText(categorySlug)) {
            ArticleCategory category = articleCategoryMapper.selectOne(
                    new LambdaQueryWrapper<ArticleCategory>()
                            .eq(ArticleCategory::getSlug, categorySlug));
            if (category == null) {
                return articleVOAssembler.emptyPage(page, size);
            }
            wrapper.eq(Article::getCategoryId, category.getId());
        }

        if (StringUtils.hasText(keyword)) {
            String likeKeyword = keyword.trim();
            wrapper.and(w -> w.like(Article::getTitle, likeKeyword)
                    .or()
                    .like(Article::getSummary, likeKeyword));
        }

        Page<Article> articlePage = articleMapper.selectPage(new Page<>(page, size), wrapper);
        return articleVOAssembler.toVoPage(articlePage);
    }
}
