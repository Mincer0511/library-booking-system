package com.gxnu.library.service;

import com.gxnu.library.dto.LoginDTO;
import com.gxnu.library.dto.PasswordUpdateDTO;
import com.gxnu.library.dto.ProfileUpdateDTO;
import com.gxnu.library.dto.RegisterDTO;
import com.gxnu.library.dto.ResetPasswordDTO;
import com.gxnu.library.dto.SendCodeDTO;
import com.gxnu.library.vo.LoginVO;
import com.gxnu.library.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户 Service
 */
public interface UserService {

    /**
     * 学生注册
     */
    void register(RegisterDTO dto);

    /**
     * 登录（学号 + 密码）
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     */
    UserInfoVO currentUser();

    /**
     * 找回密码-发送模拟验证码（6位数字，5分钟有效，返回值附带验证码供演示）
     */
    Map<String, Object> sendForgetCode(SendCodeDTO dto);

    /**
     * 找回密码-验证码校验通过后重置密码
     */
    void resetPasswordByCode(ResetPasswordDTO dto);

    /**
     * 修改个人资料（昵称/手机号）
     */
    void updateProfile(ProfileUpdateDTO dto);

    /**
     * 修改登录密码（校验原密码）
     */
    void updatePassword(PasswordUpdateDTO dto);

    /**
     * 上传头像，返回可访问的静态资源路径
     */
    String updateAvatar(MultipartFile file);
}
