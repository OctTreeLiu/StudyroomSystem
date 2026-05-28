package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.service.PriceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 价格配置控制器（用户端，只读）
 */
@RestController
@RequestMapping("/price-config")
public class PriceConfigController extends BaseController {
    
    @Autowired
    private PriceConfigService priceConfigService;
    
    /**
     * 获取预约每小时价格（用户端）
     */
    @GetMapping("/reservation-price")
    public Result<Map<String, Object>> getReservationPrice() {
        try {
            BigDecimal price = priceConfigService.getReservationPricePerHour();
            Map<String, Object> result = new HashMap<>();
            result.put("pricePerHour", price);
            result.put("pricePerHourText", price + "元/小时");
            return success(result);
        } catch (Exception e) {
            return error("获取价格信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取长期租赁每天价格（用户端）
     */
    @GetMapping("/lease-price")
    public Result<Map<String, Object>> getLeasePrice() {
        try {
            BigDecimal price = priceConfigService.getLongLeasePricePerDay();
            Map<String, Object> result = new HashMap<>();
            result.put("pricePerDay", price);
            result.put("pricePerDayText", price + "元/天");
            return success(result);
        } catch (Exception e) {
            return error("获取价格信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取会员价格和积分信息（用户端）
     */
    @GetMapping("/member-info")
    public Result<Map<String, Object>> getMemberInfo() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // VIP信息
            BigDecimal vipPrice = priceConfigService.getVipPrice();
            Integer vipPoints = priceConfigService.getVipPoints();
            BigDecimal vipDiscount = priceConfigService.getVipDiscount();
            
            // SVIP信息
            BigDecimal svipPrice = priceConfigService.getSvipPrice();
            Integer svipPoints = priceConfigService.getSvipPoints();
            BigDecimal svipDiscount = priceConfigService.getSvipDiscount();
            
            Map<String, Object> vipInfo = new HashMap<>();
            vipInfo.put("price", vipPrice);
            vipInfo.put("points", vipPoints);
            vipInfo.put("discount", vipDiscount);
            // 计算折扣文本：0.95 -> 9.5折，0.9 -> 9折，0.8 -> 8折
            BigDecimal discountPercent = vipDiscount.multiply(new BigDecimal("10"));
            // 保留一位小数，如果小数部分为0则显示整数
            String vipDiscountText = discountPercent.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "折";
            vipInfo.put("discountText", vipDiscountText);
            
            Map<String, Object> svipInfo = new HashMap<>();
            svipInfo.put("price", svipPrice);
            svipInfo.put("points", svipPoints);
            svipInfo.put("discount", svipDiscount);
            // 计算折扣文本：0.85 -> 8.5折，0.8 -> 8折
            BigDecimal svipDiscountPercent = svipDiscount.multiply(new BigDecimal("10"));
            // 保留一位小数，如果小数部分为0则显示整数
            String svipDiscountText = svipDiscountPercent.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "折";
            svipInfo.put("discountText", svipDiscountText);
            
            result.put("vip", vipInfo);
            result.put("svip", svipInfo);
            
            return success(result);
        } catch (Exception e) {
            return error("获取会员信息失败：" + e.getMessage());
        }
    }
}

