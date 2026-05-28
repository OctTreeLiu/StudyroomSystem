package com.studyspace.service;

import com.studyspace.entity.PriceConfig;
import com.studyspace.mapper.PriceConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格配置服务类
 */
@Service
public class PriceConfigService {
    
    @Autowired
    private PriceConfigMapper priceConfigMapper;
    
    // 配置键常量
    public static final String RESERVATION_PRICE_PER_HOUR = "RESERVATION_PRICE_PER_HOUR";
    public static final String LONG_LEASE_PRICE_PER_DAY = "LONG_LEASE_PRICE_PER_DAY";
    public static final String VIP_PRICE = "VIP_PRICE";
    public static final String SVIP_PRICE = "SVIP_PRICE";
    public static final String VIP_DISCOUNT = "VIP_DISCOUNT";
    public static final String SVIP_DISCOUNT = "SVIP_DISCOUNT";
    public static final String VIP_POINTS = "VIP_POINTS";
    public static final String SVIP_POINTS = "SVIP_POINTS";
    
    /**
     * 获取所有配置
     */
    public List<PriceConfig> getAllConfigs() {
        return priceConfigMapper.selectAll();
    }
    
    /**
     * 根据配置键获取配置值（字符串）
     */
    public String getConfigValue(String configKey) {
        PriceConfig config = priceConfigMapper.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
    
    /**
     * 根据配置键获取配置值（BigDecimal）
     */
    public BigDecimal getConfigValueAsBigDecimal(String configKey) {
        String value = getConfigValue(configKey);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 根据配置键获取配置值（Integer）
     */
    public Integer getConfigValueAsInteger(String configKey) {
        String value = getConfigValue(configKey);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 获取预约每小时价格
     */
    public BigDecimal getReservationPricePerHour() {
        BigDecimal price = getConfigValueAsBigDecimal(RESERVATION_PRICE_PER_HOUR);
        return price != null ? price : new BigDecimal("3"); // 默认3元
    }
    
    /**
     * 获取长期租赁每天价格
     */
    public BigDecimal getLongLeasePricePerDay() {
        BigDecimal price = getConfigValueAsBigDecimal(LONG_LEASE_PRICE_PER_DAY);
        return price != null ? price : new BigDecimal("30"); // 默认30元
    }
    
    /**
     * 获取VIP价格
     */
    public BigDecimal getVipPrice() {
        BigDecimal price = getConfigValueAsBigDecimal(VIP_PRICE);
        return price != null ? price : new BigDecimal("32.88"); // 默认32.88元
    }
    
    /**
     * 获取SVIP价格
     */
    public BigDecimal getSvipPrice() {
        BigDecimal price = getConfigValueAsBigDecimal(SVIP_PRICE);
        return price != null ? price : new BigDecimal("65.88"); // 默认65.88元
    }
    
    /**
     * 获取VIP折扣
     */
    public BigDecimal getVipDiscount() {
        BigDecimal discount = getConfigValueAsBigDecimal(VIP_DISCOUNT);
        return discount != null ? discount : new BigDecimal("0.9"); // 默认9折
    }
    
    /**
     * 获取SVIP折扣
     */
    public BigDecimal getSvipDiscount() {
        BigDecimal discount = getConfigValueAsBigDecimal(SVIP_DISCOUNT);
        return discount != null ? discount : new BigDecimal("0.8"); // 默认8折
    }
    
    /**
     * 获取VIP赠送积分
     */
    public Integer getVipPoints() {
        Integer points = getConfigValueAsInteger(VIP_POINTS);
        return points != null ? points : 150; // 默认150积分
    }
    
    /**
     * 获取SVIP赠送积分
     */
    public Integer getSvipPoints() {
        Integer points = getConfigValueAsInteger(SVIP_POINTS);
        return points != null ? points : 300; // 默认300积分
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(PriceConfig priceConfig) {
        PriceConfig existing = priceConfigMapper.selectByKey(priceConfig.getConfigKey());
        if (existing != null) {
            // 更新现有配置
            priceConfig.setId(existing.getId());
            priceConfigMapper.update(priceConfig);
        } else {
            // 插入新配置
            priceConfigMapper.insert(priceConfig);
        }
    }
    
    /**
     * 批量更新配置
     */
    public void updateConfigs(List<PriceConfig> configs) {
        for (PriceConfig config : configs) {
            updateConfig(config);
        }
    }
    
    /**
     * 获取所有配置的Map（用于前端展示）
     */
    public Map<String, Object> getAllConfigsAsMap() {
        List<PriceConfig> configs = getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        
        // 将配置列表转换为Map，方便前端使用
        Map<String, PriceConfig> configMap = configs.stream()
            .collect(Collectors.toMap(PriceConfig::getConfigKey, config -> config));
        
        result.put("configs", configs);
        result.put("configMap", configMap);
        
        return result;
    }
}

