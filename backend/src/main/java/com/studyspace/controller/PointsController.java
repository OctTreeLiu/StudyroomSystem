package com.studyspace.controller;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.entity.Points;
import com.studyspace.service.PointsService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分控制器（用户端）
 */
@RestController
@RequestMapping("/points")
public class PointsController extends BaseController {
    
    @Autowired
    private PointsService pointsService;
    
    /**
     * 获取用户当前总积分
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getPointsInfo(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            Integer totalPoints = pointsService.getUserTotalPoints(userId);
            boolean hasSignedIn = pointsService.checkDailySignIn(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalPoints", totalPoints);
            result.put("hasSignedIn", hasSignedIn);
            result.put("exchangeableHours", (totalPoints / 30) * 2); // 可兑换时长
            
            return success(result);
        } catch (Exception e) {
            return error("获取积分信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询用户积分流水记录
     */
    @GetMapping("/history")
    public Result<PageResult<Points>> getPointsHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            PageResult<Points> pageResult = pointsService.getPointsHistory(userId, page, pageSize);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询积分流水失败：" + e.getMessage());
        }
    }
    
    /**
     * 每日签到，增加1积分
     */
    @PostMapping("/sign-in")
    public Result<Map<String, Object>> dailySignIn(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            pointsService.dailySignIn(userId);
            
            Integer totalPoints = pointsService.getUserTotalPoints(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("totalPoints", totalPoints);
            result.put("message", "签到成功，获得1积分");
            
            return success("签到成功", result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("签到失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用积分兑换预约时长（仅计算所需积分，不创建预约）
     */
    @PostMapping("/exchange/calculate")
    public Result<Map<String, Object>> calculateExchangePoints(
            @RequestHeader("Authorization") String token,
            @RequestParam("hours") Integer hours) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (hours == null || hours <= 0) {
                return error("预约时长必须大于0");
            }
            
            // 检查是否以2小时的倍数兑换
            if (hours % 2 != 0) {
                return error("必须是两个小时的整数倍");
            }
            
            // 计算所需积分：30积分 * (小时数/2)
            int requiredPoints = (hours / 2) * 30;
            
            // 获取用户当前积分
            Integer totalPoints = pointsService.getUserTotalPoints(userId);
            
            // 检查积分是否足够
            boolean canExchange = pointsService.canExchangeWithPoints(hours, totalPoints);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hours", hours);
            result.put("requiredPoints", requiredPoints);
            result.put("currentPoints", totalPoints);
            result.put("canExchange", canExchange);
            result.put("remainingPoints", canExchange ? totalPoints - requiredPoints : totalPoints);
            
            if (!canExchange) {
                return error(String.format("积分不足，当前积分：%d，需要积分：%d", totalPoints, requiredPoints));
            }
            
            return success(result);
        } catch (Exception e) {
            return error("计算兑换积分失败：" + e.getMessage());
        }
    }
}

