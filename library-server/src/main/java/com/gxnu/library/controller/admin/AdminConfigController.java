package com.gxnu.library.controller.admin;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.common.result.Result;
import com.gxnu.library.entity.SysConfig;
import com.gxnu.library.service.ConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 超管专属：系统设置（预约与信用规则参数化读取/修改，修改后实时生效）
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@RequireRole(RoleKey.SUPER_ADMIN)
public class AdminConfigController {

    private final ConfigService configService;

    @Data
    public static class ConfigUpdateDTO {
        @NotBlank(message = "配置键不能为空")
        private String configKey;

        @NotBlank(message = "配置值不能为空")
        @Pattern(regexp = "^\\d+$", message = "配置值必须为非负整数")
        private String configValue;
    }

    /** 全部配置列表 */
    @GetMapping("/list")
    public Result<List<SysConfig>> list() {
        return Result.success(configService.listAll());
    }

    /** 修改配置值（清除缓存，规则实时生效） */
    @OperationLog(module = "系统设置", content = "修改系统配置")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ConfigUpdateDTO dto) {
        configService.updateValue(dto.getConfigKey(), dto.getConfigValue());
        return Result.success();
    }
}
