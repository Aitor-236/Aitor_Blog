package com.aitor.blog.article.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitor.blog.article.dto.ArticleDTO;
import com.aitor.blog.article.dto.ArticleDeleteResult;
import com.aitor.blog.article.dto.ArticleDetailVO;
import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.AdminArticleMapper;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.aitor.blog.article.service.AdminArticleService;
import com.aitor.blog.common.exception.BusinessException;
import com.aitor.blog.common.utils.PageParam;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class AdminArticleServiceImpl implements AdminArticleService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";

    private final AdminArticleMapper adminArticleMapper;
    private final ArticleCategoryMapper articleCategoryMapper;
    private final ArticleVOAssembler articleVOAssembler;

    @Override
    public Page<ArticleVO> listAll(long page, long size, String category, String keyword) {
        size = PageParam.requireValid(page, size);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getUpdatedAt);

        if (StringUtils.hasText(category)) {
            ArticleCategory matched = resolveCategory(category);
            if (matched == null) {
                return articleVOAssembler.emptyPage(page, size);
            }
            wrapper.eq(Article::getCategoryId, matched.getId());
        }

        if (StringUtils.hasText(keyword)) {
            String likeKeyword = keyword.trim();
            wrapper.and(w -> w.like(Article::getTitle, likeKeyword)
                    .or()
                    .like(Article::getSummary, likeKeyword));
        }

        Page<Article> articlePage = adminArticleMapper.selectPage(new Page<>(page, size), wrapper);
        return articleVOAssembler.toVoPage(articlePage);
    }

    @Override
    public ArticleVO createArticle(ArticleDTO articleDTO) {
        if (articleDTO == null || !StringUtils.hasText(articleDTO.getTitle())) {
            throw new BusinessException("文章标题不能为空");
        }

        ArticleCategory category = resolveCategory(articleDTO.getCategoryName());
        if (category == null) {
            throw new BusinessException("文章分类不存在");
        }

        Article article = new Article();
        article.setTitle(articleDTO.getTitle().trim());
        article.setSummary(articleDTO.getSummary());
        article.setContentMarkdown(articleDTO.getContent());
        article.setCategoryId(category.getId());
        article.setStatus(STATUS_DRAFT);

        LocalDateTime now = LocalDateTime.now();
        article.setCreatedAt(now);
        article.setUpdatedAt(now);

        adminArticleMapper.insert(article);
        return ArticleVO.from(article, category.getName());
    }

    /**
     * 根据分类名称（或英文标识）查询分类，未提供分类名称时返回 null。
     */
    private ArticleCategory resolveCategory(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        String keyword = categoryName.trim();
        ArticleCategory category = articleCategoryMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategory>()
                        .eq(ArticleCategory::getName, keyword)
                        .last("limit 1"));
        if (category == null) {
            category = articleCategoryMapper.selectOne(
                    new LambdaQueryWrapper<ArticleCategory>()
                            .eq(ArticleCategory::getSlug, keyword)
                            .last("limit 1"));
        }
        return category;
    }

    @Override
    public ArticleVO updateArticle(ArticleDTO articleDTO) {
        if (articleDTO == null || articleDTO.getId() == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article existing = adminArticleMapper.selectById(articleDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }

        Article article = new Article();
        article.setId(existing.getId());
        if (StringUtils.hasText(articleDTO.getTitle())) {
            article.setTitle(articleDTO.getTitle().trim());
        }
        if (articleDTO.getSummary() != null) {
            article.setSummary(articleDTO.getSummary());
        }
        if (articleDTO.getContent() != null) {
            article.setContentMarkdown(articleDTO.getContent());
        }
        if (StringUtils.hasText(articleDTO.getCategoryName())) {
            ArticleCategory category = resolveCategory(articleDTO.getCategoryName());
            if (category == null) {
                throw new BusinessException("文章分类不存在");
            }
            article.setCategoryId(category.getId());
        }
        article.setUpdatedAt(LocalDateTime.now());

        adminArticleMapper.updateById(article);
        return loadArticleVO(existing.getId());
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        if (id == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article article = adminArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return new ArticleDetailVO(article, loadCategory(article.getCategoryId()));
    }

    @Override
    public ArticleVO publishArticle(Long id) {
        if (id == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article existing = adminArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }
        // 已经发布过的不重复动作，避免刷新发布时间把文章顶到前台列表最前面。
        if (STATUS_PUBLISHED.equals(existing.getStatus())) {
            return loadArticleVO(id);
        }
        // article 表的标题、摘要、正文都是 NOT NULL，缺内容就发不出完整文章。
        if (!StringUtils.hasText(existing.getTitle())
                || !StringUtils.hasText(existing.getSummary())
                || !StringUtils.hasText(existing.getContentMarkdown())) {
            throw new BusinessException("文章标题、摘要和正文都不为空才能发布");
        }

        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setId(existing.getId());
        article.setStatus(STATUS_PUBLISHED);
        article.setPublishedAt(now);
        article.setUpdatedAt(now);
        adminArticleMapper.updateById(article);

        return loadArticleVO(id);
    }

    @Override
    public ArticleDeleteResult deleteArticle(Long id) {
        if (id == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article existing = adminArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }

        // 已发布的文章不允许物理删除，先取消发布回退为草稿。
        if (STATUS_PUBLISHED.equals(existing.getStatus())) {
            Article article = new Article();
            article.setId(existing.getId());
            article.setStatus(STATUS_DRAFT);
            article.setUpdatedAt(LocalDateTime.now());
            adminArticleMapper.updateById(article);
            return ArticleDeleteResult.UNPUBLISHED;
        }

        // 草稿状态直接删除，关联的 article_tag 由外键级联清理。
        adminArticleMapper.deleteById(id);
        return ArticleDeleteResult.DELETED;
    }

    @Override
    public void forceDeleteArticle(Long id) {
        if (id == null) {
            throw new BusinessException("文章ID不能为空");
        }

        Article existing = adminArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }
        // 物理删除只对草稿开放，已发布的必须先取消发布，避免误删线上文章。
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new BusinessException("只有草稿可以彻底删除，已发布的文章请先取消发布");
        }

        // 关联的 article_tag 由外键 ON DELETE CASCADE 清理。
        adminArticleMapper.deleteById(id);
    }

    /**
     * 按ID读取文章并转换为展示对象，文章不存在时返回 null。
     */
    private ArticleVO loadArticleVO(Long id) {
        Article article = adminArticleMapper.selectById(id);
        if (article == null) {
            return null;
        }
        ArticleCategory category = loadCategory(article.getCategoryId());
        return ArticleVO.from(article, category == null ? null : category.getName());
    }

    /**
     * 按ID读取分类，分类ID为空或分类已被删除时返回 null。
     */
    private ArticleCategory loadCategory(Long categoryId) {
        return categoryId == null ? null : articleCategoryMapper.selectById(categoryId);
    }
}
