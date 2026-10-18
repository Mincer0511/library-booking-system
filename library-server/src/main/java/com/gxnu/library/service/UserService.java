package com.gxnu.library.service;

import com.gxnu.library.dto.LoginDTO;
import com.gxnu.library.dto.RegisterDTO;
import com.gxnu.library.vo.LoginVO;
import com.gxnu.library.vo.UserInfoVO;

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
}
