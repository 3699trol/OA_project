package com.recruitment.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.system.dto.UserUpdateRequest;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.service.SysUserService;
import com.recruitment.system.vo.SysUserVO;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired(required = false)
    private SysUserService sysUserService;

    @GetMapping("/list")
    public Result<PageResult<SysUserVO>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                               @RequestParam(name = "size", defaultValue = "10") long size,
                                               @RequestParam(name = "keyword", required = false) String keyword,
                                               @RequestParam(name = "searchField", defaultValue = "all") String searchField,
                                               @RequestParam(name = "userType", required = false) Integer userType,
                                               @RequestParam(name = "status", required = false) Integer status,
                                               @RequestParam(name = "deleted", required = false) Integer deleted) {
        if (sysUserService == null) return Result.success(PageResult.empty(page, size));
        Page<SysUser> userPage = sysUserService.listByPage(
                page, size, keyword, searchField, userType, status, deleted);
        List<SysUserVO> voList = userPage.getRecords().stream().map(user -> {
            SysUserVO vo = new SysUserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(new PageResult<>(voList, userPage.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<SysUserVO> getById(@PathVariable("id") Long id) {
        if (sysUserService == null) return Result.success();
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable("id") Long id) {
        log.info("重置密码请求，用户ID: {}", id);
        if (sysUserService == null) {
            log.error("SysUserService 未注入，无法执行重置密码操作");
            return Result.error(500, "用户服务未初始化");
        }
        try {
            sysUserService.resetPassword(id);
            log.info("用户 {} 密码重置成功", id);
            return Result.success();
        } catch (Exception e) {
            log.error("重置密码失败，用户ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        log.info("删除用户请求，用户ID: {}", id);
        if (sysUserService == null) {
            log.error("SysUserService 未注入，无法执行删除操作");
            return Result.error(500, "用户服务未初始化");
        }
        try {
            sysUserService.deleteUser(id);
            log.info("用户 {} 删除成功", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除用户失败，用户ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}/restore")
    public Result<Void> restoreUser(@PathVariable("id") Long id) {
        log.info("恢复用户请求，用户ID: {}", id);
        if (sysUserService == null) {
            log.error("SysUserService 未注入，无法执行恢复操作");
            return Result.error(500, "用户服务未初始化");
        }
        try {
            sysUserService.restoreUser(id);
            log.info("用户 {} 恢复成功", id);
            return Result.success();
        } catch (Exception e) {
            log.error("恢复用户失败，用户ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable("id") Long id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        log.info("更新用户请求，用户ID: {}", id);
        if (sysUserService == null) {
            log.error("SysUserService 未注入，无法执行更新操作");
            return Result.error(500, "用户服务未初始化");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName(request.getRealName());
        user.setUserType(request.getUserType());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        try {
            sysUserService.updateUser(user);
            log.info("用户 {} 更新成功", id);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户失败，用户ID: {}", id, e);
            throw e;
        }
    }
}
