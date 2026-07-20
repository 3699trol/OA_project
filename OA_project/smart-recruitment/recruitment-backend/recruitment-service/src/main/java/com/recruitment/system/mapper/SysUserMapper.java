package com.recruitment.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 手写 SQL 查询用户（包含已被逻辑删除的用户）
     * 手写 SQL 不会被 MyBatis-Plus 自动追加逻辑删除条件
     */
    @Select("<script>" +
            "SELECT * FROM sys_user " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "(username LIKE CONCAT('%',#{keyword},'%') " +
            "OR real_name LIKE CONCAT('%',#{keyword},'%') " +
            "OR phone LIKE CONCAT('%',#{keyword},'%') " +
            "OR email LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY deleted ASC, create_time DESC" +
            "</script>")
    Page<SysUser> selectPageIncludeDeleted(Page<SysUser> page, @Param("keyword") String keyword);

    /**
     * 手写 SQL 恢复用户（将 deleted 置为 0），完全绕开逻辑删除的自动过滤条件
     */
    @Update("UPDATE sys_user SET deleted = 0 WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
