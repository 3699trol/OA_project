package com.recruitment.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.service.SysUserService;
import com.recruitment.system.vo.SysUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/system/user")
public class UserController {

    @Autowired(required = false)
    private SysUserService sysUserService;

    @GetMapping("/list")
    public Result<PageResult<SysUserVO>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                               @RequestParam(name = "size", defaultValue = "10") long size,
                                               @RequestParam(name = "keyword", required = false) String keyword) {
        if (sysUserService == null) return Result.success(PageResult.empty(page, size));
        Page<SysUser> userPage = sysUserService.listByPage(page, size, keyword);
        List<SysUserVO> voList = userPage.getRecords().stream().map(user -> {
            SysUserVO vo = new SysUserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(new PageResult<>(voList, userPage.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<SysUserVO> getById(@PathVariable Long id) {
        if (sysUserService == null) return Result.success();
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }
}
