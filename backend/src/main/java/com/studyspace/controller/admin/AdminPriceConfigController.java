package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.PriceConfig;
import com.studyspace.service.PriceConfigService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员价格配置控制器
 */
@RestController
@RequestMapping("/admin/price-config")
public class AdminPriceConfigController extends BaseController {
    
    @Autowired
    private PriceConfigService priceConfigService;
    
    /**
     * 获取所有价格配置
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getAllConfigs(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            Map<String, Object> result = priceConfigService.getAllConfigsAsMap();
            return success(result);
        } catch (Exception e) {
            return error("获取价格配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新单个配置
     */
    @PutMapping("/update")
    public Result<Void> updateConfig(@RequestHeader("Authorization") String token,
                                     @RequestBody PriceConfig priceConfig) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            // 验证必填字段
            if (priceConfig.getConfigKey() == null || priceConfig.getConfigKey().trim().isEmpty()) {
                return error("配置键不能为空");
            }
            
            if (priceConfig.getConfigValue() == null || priceConfig.getConfigValue().trim().isEmpty()) {
                return error("配置值不能为空");
            }
            
            // 验证数值类型配置
            String configKey = priceConfig.getConfigKey();
            if (configKey.contains("PRICE") || configKey.contains("DISCOUNT")) {
                try {
                    double value = Double.parseDouble(priceConfig.getConfigValue().trim());
                    // 折扣必须在0-1之间
                    if (configKey.contains("DISCOUNT")) {
                        if (value < 0 || value > 1) {
                            return error("折扣值必须在0-1之间");
                        }
                        // 限制两位小数
                        if (priceConfig.getConfigValue().contains(".")) {
                            String[] parts = priceConfig.getConfigValue().split("\\.");
                            if (parts.length > 1 && parts[1].length() > 2) {
                                return error("折扣值最多保留两位小数");
                            }
                        }
                    }
                    // 价格必须大于0
                    if (configKey.contains("PRICE")) {
                        if (value <= 0) {
                            return error("价格必须大于0");
                        }
                    }
                } catch (NumberFormatException e) {
                    return error("配置值必须是有效的数字");
                }
            }
            
            // 验证积分类型配置
            if (configKey.contains("POINTS")) {
                try {
                    int value = Integer.parseInt(priceConfig.getConfigValue().trim());
                    if (value < 0) {
                        return error("积分值不能为负数");
                    }
                } catch (NumberFormatException e) {
                    return error("积分值必须是有效的整数");
                }
            }
            
            priceConfigService.updateConfig(priceConfig);
            return success("配置更新成功", null);
        } catch (Exception e) {
            return error("更新配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batch-update")
    public Result<Void> batchUpdateConfigs(@RequestHeader("Authorization") String token,
                                           @RequestBody List<PriceConfig> configs) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            // 验证每个配置
            for (PriceConfig config : configs) {
                if (config.getConfigKey() == null || config.getConfigKey().trim().isEmpty()) {
                    return error("配置键不能为空");
                }
                if (config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
                    return error("配置值不能为空");
                }
            }
            
            priceConfigService.updateConfigs(configs);
            return success("批量更新配置成功", null);
        } catch (Exception e) {
            return error("批量更新配置失败：" + e.getMessage());
        }
    }
}

