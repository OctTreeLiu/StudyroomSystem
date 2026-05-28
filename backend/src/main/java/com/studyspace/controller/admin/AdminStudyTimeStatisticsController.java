package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.service.StudyTimeStatisticsService;
import com.studyspace.utils.JwtUtil;
import com.studyspace.vo.StudyTimeRankingVO;
import com.studyspace.vo.StudyTimeStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学习时长统计管理控制器（管理员接口）
 */
@RestController
@RequestMapping("/admin/study-time")
public class AdminStudyTimeStatisticsController extends BaseController {
    
    @Autowired
    private StudyTimeStatisticsService studyTimeStatisticsService;
    
    /**
     * 获取所有用户的学习时长统计（管理员）
     */
    @GetMapping("/statistics")
    public Result<StudyTimeStatisticsVO> getAllUsersStatistics(@RequestHeader("Authorization") String token) {
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
            
            StudyTimeStatisticsVO statistics = studyTimeStatisticsService.getAllUsersStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取学习时长统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户的学习时长统计（按时间段：daily/weekly/monthly）
     */
    @GetMapping("/statistics/{period}")
    public Result<StudyTimeStatisticsVO> getAllUsersStatisticsByPeriod(
            @RequestHeader("Authorization") String token,
            @PathVariable("period") String period) {
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
            
            StudyTimeStatisticsVO statistics = studyTimeStatisticsService.getAllUsersStatisticsByPeriod(period);
            return success(statistics);
        } catch (Exception e) {
            return error("获取学习时长统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取学习红人榜
     */
    @GetMapping("/ranking/{period}")
    public Result<StudyTimeRankingVO> getStudyTimeRanking(
            @RequestHeader("Authorization") String token,
            @PathVariable("period") String period) {
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
            
            StudyTimeRankingVO ranking = studyTimeStatisticsService.getStudyTimeRanking(period);
            return success(ranking);
        } catch (Exception e) {
            return error("获取学习红人榜失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户过去24小时的每小时统计（管理员）
     */
    @GetMapping("/statistics/24hours")
    public Result<java.util.List<java.util.Map<String, Object>>> getAllUsers24HoursStatistics(@RequestHeader("Authorization") String token) {
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
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getAllUsers24HoursStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取24小时统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户过去7天的每日统计（管理员）
     */
    @GetMapping("/statistics/7days")
    public Result<java.util.List<java.util.Map<String, Object>>> getAllUsers7DaysStatistics(@RequestHeader("Authorization") String token) {
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
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getAllUsers7DaysStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取7天统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定用户的学习时长统计（管理员）
     */
    @GetMapping("/statistics/user/{userId}")
    public Result<StudyTimeStatisticsVO> getUserStatistics(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") Long userId) {
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
            
            StudyTimeStatisticsVO statistics = studyTimeStatisticsService.getUserStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取用户学习时长统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定用户过去24小时的每小时统计（管理员）
     */
    @GetMapping("/statistics/user/{userId}/24hours")
    public Result<java.util.List<java.util.Map<String, Object>>> getUser24HoursStatistics(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") Long userId) {
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
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getUser24HoursStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取用户24小时统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定用户过去7天的每日统计（管理员）
     */
    @GetMapping("/statistics/user/{userId}/7days")
    public Result<java.util.List<java.util.Map<String, Object>>> getUser7DaysStatistics(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") Long userId) {
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
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getUser7DaysStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取用户7天统计失败：" + e.getMessage());
        }
    }
}

