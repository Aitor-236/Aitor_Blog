package com.aitor.blog.article.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.aitor.blog.article.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper 
public interface AdminArticleMapper extends BaseMapper<Article> {
    
}
