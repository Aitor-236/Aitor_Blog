package com.aitor.blog.article.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.aitor.blog.article.entity.ArticleTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * 查询一篇文章的标签名称，按标签ID升序。
     * <p>
     * article_tag 是复合主键表，BaseMapper 的单主键方法用不了，
     * 这里用一条 join 把标签名直接取出来，省掉两次查询。
     */
    @Select("SELECT t.name FROM tag t "
            + "JOIN article_tag atg ON atg.tag_id = t.id "
            + "WHERE atg.article_id = #{articleId} "
            + "ORDER BY t.id")
    List<String> selectTagNamesByArticleId(@Param("articleId") Long articleId);
}
