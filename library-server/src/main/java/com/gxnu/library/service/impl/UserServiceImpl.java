package com.gxnu.library.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.dto.LoginDTO;
import com.gxnu.library.dto.PasswordUpdateDTO;
import com.gxnu.library.dto.ProfileUpdateDTO;
import com.gxnu.library.dto.RegisterDTO;
import com.gxnu.library.dto.ResetPasswordDTO;
import com.gxnu.library.dto.SendCodeDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 头像允许的图片格式 */
    private static final Set<String> AVATAR_TYPES = Set.of("jpg", "jpeg", "png", "webp");
    /** 头像大小上限 2MB */
    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    /** 模拟验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtils jwtUtils;

    /** 找回密码模拟验证码存储：学号 -> (验证码, 过期时间) */
    private final Map<String, CodeEntry> forgetCodes = new ConcurrentHashMap<>();

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

    @Override
    public Map<String, Object> sendForgetCode(SendCodeDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("该学号未注册");
        }
        if (user.getPhone() == null || !user.getPhone().equals(dto.getPhone())) {
            throw new BusinessException("手机号与账号绑定信息不一致");
        }
        String code = RandomUtil.randomNumbers(6);
        forgetCodes.put(dto.getUsername(),
                new CodeEntry(code, LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES)));
        // 开发环境模拟验证码：接口回显，不接入真实短信通道
        return Map.of(
                "code", code,
                "expireMinutes", CODE_EXPIRE_MINUTES,
                "tip", "模拟验证码已生成（演示环境直接回显，生产环境将发送至手机）");
    }

    @Override
    public void resetPasswordByCode(ResetPasswordDTO dto) {
        CodeEntry entry = forgetCodes.get(dto.getUsername());
        if (entry == null || !entry.code().equals(dto.getCode())) {
            throw new BusinessException("验证码错误，请重新获取");
        }
        if (LocalDateTime.now().isAfter(entry.expireTime())) {
            forgetCodes.remove(dto.getUsername());
            throw new BusinessException("验证码已过期，请重新获取");
        }
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("该学号未注册");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);
        forgetCodes.remove(dto.getUsername());
    }

    @Override
    public void updateProfile(ProfileUpdateDTO dto) {
        Long userId = UserContext.currentUserId();
        SysUser user = userMapper.selectById(userId);
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {
        Long userId = UserContext.currentUserId();
        SysUser user = userMapper.selectById(userId);
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public String updateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像图片");
        }
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException("头像图片不能超过2MB");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!AVATAR_TYPES.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/webp 格式图片");
        }
        Long userId = UserContext.currentUserId();
        String fileName = userId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + ext;
        Path dir = Paths.get("uploads", "avatar");
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName));
        } catch (IOException e) {
            throw new BusinessException("头像上传失败，请重试");
        }
        String url = "/uploads/avatar/" + fileName;
        SysUser user = userMapper.selectById(userId);
        user.setAvatar(url);
        userMapper.updateById(user);
        return url;
    }

    /** 模拟验证码条目 */
    private record CodeEntry(String code, LocalDateTime expireTime) {
    }
}
