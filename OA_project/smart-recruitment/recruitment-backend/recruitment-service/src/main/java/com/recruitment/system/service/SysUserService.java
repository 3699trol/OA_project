package com.recruitment.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 分页查询用户列表，支持关键字筛选
     */
    Page<SysUser> listByPage(long pageNum, long pageSize, String keyword);

    /**
     * 根据ID查询用户
     */
    SysUser getById(Long id);
}
