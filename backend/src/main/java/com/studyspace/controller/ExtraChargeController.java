package com.studyspace.controller;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.entity.ExtraChargeOrder;
import com.studyspace.service.AlipayService;
import com.studyspace.service.ExtraChargeOrderService;
import com.studyspace.config.AlipayConfig;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 额外收费订单控制器（用户端）
 */
@RestController
@RequestMapping("/extra-charge")
public class ExtraChargeController extends BaseController {
    
    @Autowired
    private ExtraChargeOrderService extraChargeOrderService;
    
    @Autowired
    private AlipayService alipayService;
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    /**
     * 获取我的收费订单列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<ExtraChargeOrder>> getMyOrders(@RequestHeader("Authorization") String token,
                                                             @RequestParam(required = false, defaultValue = "1") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token");
            }
            
            PageResult<ExtraChargeOrder> result = extraChargeOrderService.getOrdersByUserId(userId, page, pageSize);
            return success(result);
        } catch (Exception e) {
            return error("获取订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建支付订单（返回支付表单HTML）
     */
    @PostMapping("/pay/{id}")
    public Result<String> createPayment(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id,
                                        @RequestHeader(value = "Origin", required = false) String origin) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token");
            }
            
            ExtraChargeOrder order = extraChargeOrderService.getById(id);
            if (order == null) {
                return error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId)) {
                return error("无权支付该订单");
            }
            
            if (order.getPaymentStatus() == 1) {
                return error("该订单已支付");
            }
            
            if (order.getPaymentStatus() == 2) {
                return error("该订单已取消，无法支付");
            }
            
            // 检查是否超过24小时
            long hoursSinceCreation = java.time.Duration.between(order.getCreateTime(), java.time.LocalDateTime.now()).toHours();
            if (hoursSinceCreation >= 24) {
                return error("已超过付款截止时间，无法支付");
            }
            
            // 创建支付订单
            String subject = "额外收费 - " + order.getContent();
            String body = "订单编号：" + order.getOrderNumber() + "，收费内容：" + order.getContent();
            
            // 获取前端域名
            String frontBase = origin != null ? origin : alipayConfig.getFrontendBaseUrl();
            
            String paymentForm = alipayService.createPayment(
                order.getOrderNumber(),
                order.getAmount(),
                subject,
                body,
                frontBase
            );
            
            return success("支付表单生成成功", paymentForm);
        } catch (Exception e) {
            return error("创建支付订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 统计用户未支付订单数量（用于菜单高亮）
     */
    @GetMapping("/count-unpaid")
    public Result<Map<String, Long>> countUnpaidOrders(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token");
            }
            
            long count = extraChargeOrderService.countUnpaidOrdersByUserId(userId);
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);
            return success(result);
        } catch (Exception e) {
            return error("统计失败：" + e.getMessage());
        }
    }

    /**
     * 用户主动取消未支付订单
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancelOrder(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);

            if (userId == null) {
                return error(401, "无效的token");
            }

            extraChargeOrderService.cancelOrder(id, userId);
            return success("取消成功");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("取消失败：" + e.getMessage());
        }
    }
}

