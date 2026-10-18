package com.gxnu.library.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录返回
 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;
    private UserInfoVO userInfo;
}
