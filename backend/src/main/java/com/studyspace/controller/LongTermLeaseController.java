package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.dto.LongTermLeaseDTO;
import com.studyspace.entity.LongTermLease;
import com.studyspace.service.AlipayService;
import com.studyspace.service.LongTermLeaseService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 长期租赁控制器
 */
@RestController
@RequestMapping("/lease")
public class LongTermLeaseController extends BaseController {
    
    @Autowired
    private LongTermLeaseService longTermLeaseService;

    @Autowired
    private AlipayService alipayService;
    
    /**
     * 创建长期租赁申请
     */
    @PostMapping("/apply")
    public Result<LongTermLease> applyLease(@RequestHeader("Authorization") String token,
                                             @Validated @RequestBody LongTermLeaseDTO leaseDTO) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            LongTermLease lease = longTermLeaseService.createLeaseApplication(userId, leaseDTO);
            return success("长期租赁申请提交成功，系统已自动审核通过，请在2小时内完成付款", lease);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("提交申请失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询当前用户的租赁列表
     */
    @GetMapping("/my")
    public Result<List<LongTermLease>> getMyLeases(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            List<LongTermLease> leases = longTermLeaseService.getLeasesByUserId(userId);
            return success(leases);
        } catch (Exception e) {
            return error("查询租赁列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否有已生效的长期租赁订单（用于预约冲突检查）
     * 注意：此接口检查当前日期是否有长期租赁，建议使用 check-active-by-time 接口
     */
    @GetMapping("/check-active")
    public Result<LongTermLease> checkActiveLease(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            LongTermLease activeLease = longTermLeaseService.getCurrentActiveLeaseByUserId(userId);
            if (activeLease != null) {
                return success(activeLease);
            } else {
                return success(null);
            }
        } catch (Exception e) {
            return error("检查长期租赁状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查用户在指定时间段内是否有生效的长期租赁订单（用于预约冲突检查）
     * 优化逻辑：只在所选时间段内检查是否有生效的长期租赁，允许预约长期租赁结束之后的日期
     */
    @GetMapping("/check-active-by-time")
    public Result<LongTermLease> checkActiveLeaseByTime(@RequestHeader("Authorization") String token,
                                                         @RequestParam("startTime") String startTimeStr,
                                                         @RequestParam("endTime") String endTimeStr) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            // 解析时间参数
            java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(startTimeStr, 
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(endTimeStr, 
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            LongTermLease conflictingLease = longTermLeaseService.getActiveLeaseByUserIdAndTimeRange(userId, startTime, endTime);
            if (conflictingLease != null) {
                return success(conflictingLease);
            } else {
                return success(null);
            }
        } catch (java.time.format.DateTimeParseException e) {
            return error("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        } catch (Exception e) {
            return error("检查长期租赁状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询租赁详情
     */
    @GetMapping("/{id}")
    public Result<LongTermLease> getLeaseById(@PathVariable Long id) {
        try {
            LongTermLease lease = longTermLeaseService.getLeaseById(id);
            if (lease == null) {
                return error("租赁记录不存在");
            }
            return success(lease);
        } catch (Exception e) {
            return error("查询租赁详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建长期租赁支付订单（返回支付表单HTML）
     */
    @PostMapping("/pay/{id}")
    public Result<String> createLeasePayment(@RequestHeader("Authorization") String token,
                                             @PathVariable Long id,
                                             @RequestParam(value = "frontBase", required = false) String frontBase) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);

            if (userId == null) {
                return error("无效的token");
            }

            // 获取租赁信息
            LongTermLease lease = longTermLeaseService.getLeaseById(id);
            if (lease == null) {
                return error("租赁记录不存在");
            }

            // 检查是否为该用户的租赁
            if (!lease.getUserId().equals(userId)) {
                return error("无权支付该租赁");
            }

            // 检查租赁状态（必须是审核通过待付款）
            if (lease.getStatus() != 1) {
                return error("该租赁申请未审核通过或已支付");
            }

            // 检查是否已支付
            if (lease.getPaymentStatus() == 1) {
                return error("该租赁已支付");
            }

            // 检查是否超过付款截止时间
            if (lease.getPaymentDeadline() != null && lease.getPaymentDeadline().isBefore(java.time.LocalDateTime.now())) {
                return error("已超过付款截止时间，无法支付");
            }

            // 创建支付订单
            String outTradeNo = lease.getLeaseNumber();
            String subject = "长期租赁-" + outTradeNo;
            String body = String.format("长期租赁座位：%s %s号，时间：%s 至 %s",
                    lease.getRoomName() != null ? lease.getRoomName() : "自习室",
                    lease.getSeatNumber() != null ? lease.getSeatNumber() : "座位",
                    lease.getStartDate(), lease.getEndDate());

            String paymentForm = alipayService.createPayment(
                    outTradeNo,
                    lease.getAmount(),
                    subject,
                    body,
                    frontBase
            );

            return success("支付表单生成成功", paymentForm);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("创建支付订单失败：" + e.getMessage());
        }
    }

    /**
     * 取消已支付的长期租赁并退款
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelLease(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);

            if (userId == null) {
                return error("无效的token");
            }

            // 先检查订单状态，判断是未支付取消还是已支付退款
            LongTermLease lease = longTermLeaseService.getLeaseById(id);
            if (lease == null) {
                return error("租赁记录不存在");
            }

            // 如果是未支付状态，调用取消未支付订单的方法
            if (lease.getStatus() != null && lease.getStatus() == 1 
                && lease.getPaymentStatus() != null && lease.getPaymentStatus() == 0) {
                longTermLeaseService.cancelUnpaidLease(id, userId);
                return success("取消成功", null);
            } else {
                // 否则调用已支付订单的退款方法
                longTermLeaseService.cancelLease(id, userId);
                return success("退款成功", null);
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("取消长期租赁失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前日期在使用中的长期租赁列表（用于座位图显示，不需要登录）
     */
    @GetMapping("/active/current")
    public Result<List<LongTermLease>> getCurrentActiveLeases() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            List<LongTermLease> activeLeases = longTermLeaseService.getActiveLeasesByDateRange(today, today);
            return success(activeLeases);
        } catch (Exception e) {
            return error("查询当前使用中的长期租赁失败：" + e.getMessage());
        }
    }
}
