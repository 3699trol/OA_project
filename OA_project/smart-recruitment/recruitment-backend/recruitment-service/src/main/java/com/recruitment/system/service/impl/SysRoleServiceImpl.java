package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.system.entity.SysRole;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysRoleMapper;
import com.recruitment.system.mapper.SysUserMapper;
import com.recruitment.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<SysRole> listAll() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .orderByDesc(SysRole::getStatus)
                        .orderByAsc(SysRole::getId));

        // 通过 sys_user.user_type 统计每个角色的用户数
        // userType: 1=ADMIN, 2=HR, 3=INTERVIEWER, 4=CANDIDATE
        List<SysUser> allUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0));
        Map<Integer, Long> userTypeCountMap = allUsers.stream()
                .collect(Collectors.groupingBy(SysUser::getUserType, Collectors.counting()));

        // userType -> roleCode 映射
        Map<String, Integer> roleCodeToUserType = Map.of(
                "ADMIN", 1, "HR", 2, "INTERVIEWER", 3, "CANDIDATE", 4);

        for (SysRole role : roles) {
            Integer userType = roleCodeToUserType.get(role.getRoleCode());
            if (userType != null) {
                role.setUserCount(userTypeCountMap.getOrDefault(userType, 0L).intValue());
            } else {
                role.setUserCount(0);
            }
        }
        return roles;
    }

    @Override
    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public boolean updateRole(SysRole sysRole) {
        sysRole.setUpdateTime(java.time.LocalDateTime.now());
        return sysRoleMapper.updateById(sysRole) > 0;
    }
}
