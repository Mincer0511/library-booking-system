package com.gxnu.library.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.dto.LoginDTO;
import com.gxnu.library.dto.RegisterDTO;
import com.gxnu.library.entity.SysRole;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.mapper.SysRoleMapper;
import com.gxnu.library.mapper.SysUserMapper;
import com.gxnu.library.service.UserService;
import com.gxnu.library.utils.JwtUtils;
import com.gxnu.library.utils.UserContext;
import com.gxnu.library.vo.LoginVO;
import com.gxnu.library.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        // 学号唯一校验
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("该学号已被注册");
        }
        SysRole studentRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, RoleKey.STUDENT));

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setName(dto.getName());
        user.setNickname(dto.getName());
        user.setPhone(dto.getPhone());
        user.setRoleId(studentRole.getRoleId());
        user.setCreditScore(100);
        user.setIsDisabled(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("学号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        String token = jwtUtils.createToken(user.getUserId(), user.getUsername(), role.getRoleKey());
        return new LoginVO(token, UserInfoVO.of(user, role.getRoleKey()));
    }

    @Override
    public UserInfoVO currentUser() {
        Long userId = UserContext.currentUserId();
        SysUser user = userMapper.selectById(userId);
        SysRole role = roleMapper.selectById(user.getRoleId());
        return UserInfoVO.of(user, role.getRoleKey());
    }
}
