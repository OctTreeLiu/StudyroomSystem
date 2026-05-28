package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.service.StatisticsService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员统计控制器
 */
@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController extends BaseController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取座位总体统计信息（五个大区座位之和的使用情况）
     */
    @GetMapping("/seat")
    public Result<Map<String, Object>> getSeatStatistics(@RequestHeader("Authorization") String token) {
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
            
            Map<String, Object> statistics = statisticsService.getSeatStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取自习室统计信息（兼容旧接口，实际返回座位统计）
     */
    @GetMapping("/studyroom")
    public Result<Map<String, Object>> getStudyRoomStatistics(@RequestHeader("Authorization") String token) {
        return getSeatStatistics(token);
    }
    
    /**
     * 获取每个自习室的使用情况统计
     */
    @GetMapping("/room-usage")
    public Result<List<Map<String, Object>>> getRoomUsageStatistics(@RequestHeader("Authorization") String token) {
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
            
            List<Map<String, Object>> statistics = statisticsService.getRoomUsageStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取使用情况统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取五个大区的统计信息
     */
    @GetMapping("/area")
    public Result<List<Map<String, Object>>> getAreaStatistics(@RequestHeader("Authorization") String token) {
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
            
            List<Map<String, Object>> statistics = statisticsService.getAreaStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取大区统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据时间段获取座位统计信息
     */
    @GetMapping("/seat/time-range")
    public Result<Map<String, Object>> getSeatStatisticsByTimeRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
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
            
            if (startTime == null || endTime == null) {
                return error("开始时间和结束时间不能为空");
            }
            
            if (startTime.isAfter(endTime)) {
                return error("开始时间不能晚于结束时间");
            }
            
            Map<String, Object> statistics = statisticsService.getSeatStatisticsByTimeRange(startTime, endTime);
            return success(statistics);
        } catch (Exception e) {
            return error("获取时间段统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据时间段获取五个大区的统计信息
     */
    @GetMapping("/area/time-range")
    public Result<List<Map<String, Object>>> getAreaStatisticsByTimeRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
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
            
            if (startTime == null || endTime == null) {
                return error("开始时间和结束时间不能为空");
            }
            
            if (startTime.isAfter(endTime)) {
                return error("开始时间不能晚于结束时间");
            }
            
            List<Map<String, Object>> statistics = statisticsService.getAreaStatisticsByTimeRange(startTime, endTime);
            return success(statistics);
        } catch (Exception e) {
            return error("获取时间段大区统计信息失败：" + e.getMessage());
        }
    }
}

