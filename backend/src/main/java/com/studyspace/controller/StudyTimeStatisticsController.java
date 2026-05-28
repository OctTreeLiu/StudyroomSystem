package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.service.StudyTimeStatisticsService;
import com.studyspace.utils.JwtUtil;
import com.studyspace.vo.StudyTimeStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学习时长统计控制器（用户接口）
 */
@RestController
@RequestMapping("/study-time")
public class StudyTimeStatisticsController extends BaseController {
    
    @Autowired
    private StudyTimeStatisticsService studyTimeStatisticsService;
    
    /**
     * 获取当前用户的学习时长统计
     */
    @GetMapping("/statistics")
    public Result<StudyTimeStatisticsVO> getMyStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            StudyTimeStatisticsVO statistics = studyTimeStatisticsService.getUserStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取学习时长统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户过去24小时的每小时统计
     */
    @GetMapping("/statistics/24hours")
    public Result<java.util.List<java.util.Map<String, Object>>> getMy24HoursStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getUser24HoursStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取24小时统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户过去7天的每日统计
     */
    @GetMapping("/statistics/7days")
    public Result<java.util.List<java.util.Map<String, Object>>> getMy7DaysStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            java.util.List<java.util.Map<String, Object>> statistics = studyTimeStatisticsService.getUser7DaysStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            return error("获取7天统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取学习红人榜（普通用户）
     */
    @GetMapping("/ranking/{period}")
    public Result<com.studyspace.vo.StudyTimeRankingVO> getStudyTimeRanking(
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
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            com.studyspace.vo.StudyTimeRankingVO ranking = studyTimeStatisticsService.getStudyTimeRanking(period);
            return success(ranking);
        } catch (Exception e) {
            return error("获取学习红人榜失败：" + e.getMessage());
        }
    }
}

