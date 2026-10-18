package com.gxnu.library.vo;

import com.gxnu.library.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录用户信息（脱敏）
 */
@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String name;
    private String nickname;
    private String phone;
    private String avatar;
    private String roleKey;
    private Integer creditScore;
    private Integer isDisabled;
    private LocalDateTime disableEndTime;

    public static UserInfoVO of(SysUser user, String roleKey) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRoleKey(roleKey);
        vo.setCreditScore(user.getCreditScore());
        vo.setIsDisabled(user.getIsDisabled());
        vo.setDisableEndTime(user.getDisableEndTime());
        return vo;
    }
}
