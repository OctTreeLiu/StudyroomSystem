package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.UsageRecord;
import com.studyspace.service.UsageRecordService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 使用记录控制器
 */
@RestController
@RequestMapping("/usage")
public class UsageRecordController extends BaseController {
    
    @Autowired
    private UsageRecordService usageRecordService;
    
    /**
     * 查询当前用户的使用记录
     */
    @GetMapping("/my")
    public Result<List<UsageRecord>> getMyUsageRecords(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            List<UsageRecord> records = usageRecordService.getRecordsByUserId(userId);
            return success(records);
        } catch (Exception e) {
            return error("查询使用记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询使用记录详情
     */
    @GetMapping("/{id}")
    public Result<UsageRecord> getUsageRecordById(@PathVariable Long id) {
        try {
            UsageRecord record = usageRecordService.getRecordById(id);
            if (record == null) {
                return error("使用记录不存在");
            }
            return success(record);
        } catch (Exception e) {
            return error("查询使用记录详情失败：" + e.getMessage());
        }
    }
}

