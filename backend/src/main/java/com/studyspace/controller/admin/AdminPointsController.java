package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.Points;
import com.studyspace.service.PointsService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 积分管理控制器（管理员端）
 */
@RestController
@RequestMapping("/admin/points")
public class AdminPointsController extends BaseController {
    
    @Autowired
    private PointsService pointsService;
    
    /**
     * 分页查询所有用户积分流水记录（支持按用户名搜索）
     */
    @GetMapping("/history")
    public Result<PageResult<Points>> getAllPointsHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username) {
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
            
            PageResult<Points> pageResult = pointsService.getAllPointsHistory(page, pageSize, username);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询积分流水失败：" + e.getMessage());
        }
    }
    
    /**
     * 管理员手动调整用户积分
     */
    @PostMapping("/adjust")
    public Result<Void> adjustPoints(
            @RequestHeader("Authorization") String token,
            @RequestParam("userId") Long userId,
            @RequestParam("points") Integer points,
            @RequestParam(value = "description", required = false) String description) {
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
            
            if (points == null || points == 0) {
                return error("调整积分不能为0");
            }
            
            if (description == null || description.trim().isEmpty()) {
                description = points > 0 ? "管理员增加积分" : "管理员扣除积分";
            }
            
            pointsService.adjustPoints(userId, points, description);
            
            return success(points > 0 ? "增加积分成功" : "扣除积分成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("调整积分失败：" + e.getMessage());
        }
    }
}

