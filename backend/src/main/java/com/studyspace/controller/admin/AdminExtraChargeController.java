package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.ExtraChargeOrder;
import com.studyspace.service.ExtraChargeOrderService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员额外收费管理控制器
 */
@RestController
@RequestMapping("/admin/extra-charge")
public class AdminExtraChargeController extends BaseController {
    
    @Autowired
    private ExtraChargeOrderService extraChargeOrderService;
    
    /**
     * 发起收费申请
     */
    @PostMapping("/create")
    public Result<ExtraChargeOrder> createCharge(@RequestHeader("Authorization") String token,
                                                   @RequestBody Map<String, Object> request) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null || role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            Long userId = ((Number) request.get("userId")).longValue();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String content = (String) request.get("content");
            
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return error("收费金额必须大于0");
            }
            
            if (content == null || content.trim().isEmpty()) {
                return error("收费内容不能为空");
            }
            
            ExtraChargeOrder order = extraChargeOrderService.createOrder(userId, amount, content);
            return success("收费申请创建成功", order);
        } catch (Exception e) {
            return error("创建收费申请失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询所有收费订单（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<ExtraChargeOrder>> getAllOrders(@RequestHeader("Authorization") String token,
                                                             @RequestParam(required = false, defaultValue = "1") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null || role != 1) {
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            PageResult<ExtraChargeOrder> result = extraChargeOrderService.getAllOrders(page, pageSize);
            return success(result);
        } catch (Exception e) {
            return error("查询订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 统计未支付订单数量（用于菜单高亮）
     */
    @GetMapping("/count-unpaid")
    public Result<Map<String, Long>> countUnpaidOrders(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null || role != 1) {
                return error(403, "无权限访问");
            }
            
            long count = extraChargeOrderService.countUnpaidOrders();
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);
            return success(result);
        } catch (Exception e) {
            return error("统计失败：" + e.getMessage());
        }
    }
}

