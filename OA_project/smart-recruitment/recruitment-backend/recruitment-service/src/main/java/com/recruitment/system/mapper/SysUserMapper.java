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
            "<choose>" +
            "<when test='searchField == \"username\"'>username LIKE CONCAT('%',#{keyword},'%')</when>" +
            "<when test='searchField == \"realName\"'>real_name LIKE CONCAT('%',#{keyword},'%')</when>" +
            "<when test='searchField == \"phone\"'>phone LIKE CONCAT('%',#{keyword},'%')</when>" +
            "<when test='searchField == \"email\"'>email LIKE CONCAT('%',#{keyword},'%')</when>" +
            "<otherwise>(username LIKE CONCAT('%',#{keyword},'%') " +
            "OR real_name LIKE CONCAT('%',#{keyword},'%') " +
            "OR phone LIKE CONCAT('%',#{keyword},'%') " +
            "OR email LIKE CONCAT('%',#{keyword},'%'))</otherwise>" +
            "</choose>" +
            "</if>" +
            "<if test='userType != null'> AND user_type = #{userType}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='deleted != null'> AND deleted = #{deleted}</if>" +
            "</where>" +
            " ORDER BY deleted ASC, create_time DESC" +
            "</script>")
    Page<SysUser> selectPageIncludeDeleted(Page<SysUser> page,
                                            @Param("keyword") String keyword,
                                            @Param("searchField") String searchField,
                                            @Param("userType") Integer userType,
                                            @Param("status") Integer status,
                                            @Param("deleted") Integer deleted);

    /**
     * 手写 SQL 恢复用户（将 deleted 置为 0），完全绕开逻辑删除的自动过滤条件
     */
    @Update("UPDATE sys_user SET deleted = 0 WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
