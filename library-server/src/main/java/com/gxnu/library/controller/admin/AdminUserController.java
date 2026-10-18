package com.gxnu.library.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.page.PageResult;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.dto.UserSaveDTO;
import com.gxnu.library.dto.UserUpdateDTO;
import com.gxnu.library.entity.SysRole;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysRoleMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.AdminUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 超管专属：用户管理（增改/账号禁用/角色分配/重置密码）与角色列表
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@RequireRole(RoleKey.SUPER_ADMIN)
public class AdminUserController {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    /** 用户分页：关键词（学号/姓名模糊）+ 角色 + 状态 */
    @GetMapping("/page")
    public Result<PageResult<AdminUserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysUser>()
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getName, keyword))
                        .eq(roleId != null, SysUser::getRoleId, roleId)
                        .eq(status != null, SysUser::getStatus, status)
                        .orderByDesc(SysUser::getCreateTime));

        Map<Long, SysRole> roleMap = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getRoleId, Function.identity()));

        List<AdminUserVO> list = page.getRecords().stream().map(user -> {
            AdminUserVO vo = new AdminUserVO();
            BeanUtil.copyProperties(user, vo);
            SysRole role = roleMap.get(user.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getRoleName());
            }
            return vo;
        }).toList();

        PageResult<AdminUserVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return Result.success(result);
    }

    /** 角色列表（角色下拉选择用） */
    @GetMapping("/role/list")
    public Result<List<SysRole>> roleList() {
        return Result.success(roleMapper.selectList(null));
    }

    @OperationLog(module = "用户管理", content = "新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserSaveDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("该账号已存在");
        }
        requireRole(dto.getRoleId());

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(BCrypt.hashpw(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD));
        user.setName(dto.getName().trim());
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getName().trim());
        user.setPhone(dto.getPhone());
        user.setRoleId(dto.getRoleId());
        user.setCreditScore(100);
        user.setIsDisabled(0);
        user.setStatus(1);
        userMapper.insert(user);
        return Result.success();
    }

    @OperationLog(module = "用户管理", content = "修改用户信息")
    @PutMapping("/{userId}")
    public Result<Void> update(@PathVariable Long userId, @Valid @RequestBody UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        requireRole(Long.valueOf(dto.getRoleId()));
        // 防锁死：不允许超管修改自己的角色或禁用自己账号
        Long currentUserId = UserContext.currentUserId();
        if (userId.equals(currentUserId) && dto.getStatus() != null && dto.getStatus() != 1) {
            throw new BusinessException("不能禁用自己的账号");
        }
        if (userId.equals(currentUserId) && !Long.valueOf(dto.getRoleId()).equals(user.getRoleId())) {
            throw new BusinessException("不能修改自己的角色");
        }

        user.setName(dto.getName().trim());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setRoleId(Long.valueOf(dto.getRoleId()));
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        userMapper.updateById(user);
        return Result.success();
    }

    @OperationLog(module = "用户管理", content = "重置用户密码")
    @PutMapping("/{userId}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        user.setPassword(BCrypt.hashpw(DEFAULT_PASSWORD));
        userMapper.updateById(user);
        return Result.success();
    }

    private void requireRole(Long roleId) {
        if (roleMapper.selectById(roleId) == null) {
            throw new BusinessException("所选角色不存在");
        }
    }
}
