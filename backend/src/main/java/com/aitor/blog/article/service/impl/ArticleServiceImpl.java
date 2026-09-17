package com.aitor.blog.article.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.dto.ArticlePublicDetailVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.aitor.blog.article.mapper.ArticleMapper;
import com.aitor.blog.article.service.ArticleService;
import com.aitor.blog.auth.entity.SysUser;
import com.aitor.blog.auth.mapper.SysUserMapper;
import com.aitor.blog.common.exception.BusinessException;
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
    private final SysUserMapper sysUserMapper;

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

    @Override
    public ArticlePublicDetailVO getPublishedDetail(Long id) {
        if (id == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article article = articleMapper.selectById(id);
        // 草稿和已下架的文章对前台一律不可见
        if (article == null || !STATUS_PUBLISHED.equals(article.getStatus())) {
            throw new BusinessException(404, "文章不存在或未发布");
        }

        ArticleCategory category = article.getCategoryId() == null
                ? null
                : articleCategoryMapper.selectById(article.getCategoryId());
        return new ArticlePublicDetailVO(article, category, loadAuthorUsername(article.getAuthorId()));
    }

    /**
     * 读取作者公开名称；用户不存在时返回 null，不影响文章本身展示。
     */
    private String loadAuthorUsername(Long authorId) {
        if (authorId == null) {
            return null;
        }
        SysUser author = sysUserMapper.selectById(authorId);
        return author == null ? null : author.getUsername();
    }
}
