package com.aitor.blog.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.aitor.blog.auth.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper 
public interface SysUserMapper extends BaseMapper<SysUser> {
    
}
