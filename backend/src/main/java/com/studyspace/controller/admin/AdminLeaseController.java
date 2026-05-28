package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.dto.LeaseAuditDTO;
import com.studyspace.entity.LongTermLease;
import com.studyspace.service.LongTermLeaseService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 管理员长期租赁管理控制器
 */
@RestController
@RequestMapping("/admin/lease")
public class AdminLeaseController extends BaseController {
    
    @Autowired
    private LongTermLeaseService longTermLeaseService;
    
    /**
     * 查询所有长期租赁申请（管理员使用）
     */
    @GetMapping("/list")
    public Result<java.util.List<LongTermLease>> getAllLeases(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            java.lang.Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(java.lang.Integer.valueOf(401), "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(java.lang.Integer.valueOf(403), "无权限访问，仅管理员可查看");
            }
            
            java.util.List<LongTermLease> leases = longTermLeaseService.getAllLeases();
            return success(leases);
        } catch (Exception e) {
            return error("查询租赁列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询租赁申请
     */
    @GetMapping("/list/status/{status}")
    public Result<java.util.List<LongTermLease>> getLeasesByStatus(@RequestHeader("Authorization") String token,
                                                         @PathVariable java.lang.Integer status) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            java.lang.Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(java.lang.Integer.valueOf(401), "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(java.lang.Integer.valueOf(403), "无权限访问，仅管理员可查看");
            }
            
            java.util.List<LongTermLease> leases = longTermLeaseService.getLeasesByStatus(status);
            return success(leases);
        } catch (Exception e) {
            return error("查询租赁列表失败：" + e.getMessage());
        }
    }

    /**
     * 待审核申请数量（用于小红点提醒）
     */
    @GetMapping("/pending/count")
    public Result<java.lang.Integer> getPendingCount(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }

            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }

            java.lang.Integer role = JwtUtil.getRoleFromToken(actualToken);

            if (role == null) {
                return error(java.lang.Integer.valueOf(401), "无效的token，请重新登录");
            }

            if (role != 1) {
                return error(java.lang.Integer.valueOf(403), "无权限访问，仅管理员可查看");
            }

            java.lang.Integer count = longTermLeaseService.countPending();
            return success(count);
        } catch (Exception e) {
            return error("获取待审核数量失败：" + e.getMessage());
        }
    }
    
    /**
     * 审核长期租赁申请
     */
    @PostMapping("/audit/{id}")
    public Result<Void> auditLease(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id,
                                    @Validated @RequestBody LeaseAuditDTO auditDTO) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(java.lang.Integer.valueOf(401), "未授权，请先登录");
            }
            
            java.lang.Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(java.lang.Integer.valueOf(401), "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(java.lang.Integer.valueOf(403), "无权限访问，仅管理员可操作");
            }
            
            longTermLeaseService.auditLease(id, auditDTO.getApproved(), auditDTO.getAuditRemark());
            
            String message = auditDTO.getApproved() ? "审核通过，用户需在2小时内完成付款" : "审核已拒绝";
            return success(message, null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("审核失败：" + e.getMessage());
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
}

