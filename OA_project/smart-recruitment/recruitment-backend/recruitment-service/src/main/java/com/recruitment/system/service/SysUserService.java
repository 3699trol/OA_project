package com.recruitment.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 分页查询用户列表，支持指定字段搜索及角色、状态筛选
     */
    Page<SysUser> listByPage(long pageNum, long pageSize, String keyword, String searchField,
                             Integer userType, Integer status, Integer deleted);

    /**
     * 根据ID查询用户
     */
    SysUser getById(Long id);

    /**
     * 重置用户密码为默认密码 123456
     */
    void resetPassword(Long id);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id);

    /**
     * 恢复已删除的用户
     */
    void restoreUser(Long id);

    /**
     * 更新用户信息（仅允许更新姓名、角色、性别、手机、邮箱、状态等非敏感字段）
     */
    void updateUser(SysUser user);
}
