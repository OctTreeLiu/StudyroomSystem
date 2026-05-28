package com.studyspace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyspace.common.PageResult;
import com.studyspace.entity.User;
import com.studyspace.entity.UserModifyLog;
import com.studyspace.mapper.UserModifyLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户修改日志服务类
 */
@Service
public class UserModifyLogService {
    
    @Autowired
    private UserModifyLogMapper userModifyLogMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 记录用户修改日志
     * 触发条件：
     * 1. 修改会员身份（普通用户/VIP/SVIP）
     * 2. 修改会员剩余天数（通过修改会员到期时间实现）
     */
    @Transactional
    public void logMemberTypeChange(Long userId, User beforeUser, User afterUser, Long adminId, String adminUsername) {
        // 检查会员类型是否发生变化
        Integer beforeMemberType = beforeUser.getMemberType() != null ? beforeUser.getMemberType() : 0;
        Integer afterMemberType = afterUser.getMemberType() != null ? afterUser.getMemberType() : 0;
        boolean memberTypeChanged = !beforeMemberType.equals(afterMemberType);
        
        // 检查会员到期时间是否发生变化（即会员剩余天数是否变化）
        boolean memberExpireTimeChanged = false;
        if (beforeUser.getMemberExpireTime() == null && afterUser.getMemberExpireTime() != null) {
            memberExpireTimeChanged = true;
        } else if (beforeUser.getMemberExpireTime() != null && afterUser.getMemberExpireTime() == null) {
            memberExpireTimeChanged = true;
        } else if (beforeUser.getMemberExpireTime() != null && afterUser.getMemberExpireTime() != null) {
            memberExpireTimeChanged = !beforeUser.getMemberExpireTime().equals(afterUser.getMemberExpireTime());
        }
        
        // 如果会员类型或会员到期时间发生变化，则记录日志
        if (memberTypeChanged || memberExpireTimeChanged) {
            UserModifyLog log = new UserModifyLog();
            log.setUserId(userId);
            log.setAdminId(adminId);
            log.setAdminUsername(adminUsername);
            log.setUserUsername(afterUser.getUsername());
            log.setBeforeMemberType(beforeMemberType);
            log.setAfterMemberType(afterMemberType);
            log.setBeforeMemberExpireTime(beforeUser.getMemberExpireTime());
            log.setAfterMemberExpireTime(afterUser.getMemberExpireTime());
            
            // 构建修改详情JSON
            try {
                Map<String, Object> detail = new HashMap<>();
                Map<String, Object> before = new HashMap<>();
                before.put("memberType", getMemberTypeText(beforeMemberType));
                before.put("memberExpireTime", beforeUser.getMemberExpireTime() != null ? 
                    beforeUser.getMemberExpireTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                before.put("realName", beforeUser.getRealName());
                before.put("phone", beforeUser.getPhone());
                before.put("email", beforeUser.getEmail());
                
                Map<String, Object> after = new HashMap<>();
                after.put("memberType", getMemberTypeText(afterMemberType));
                after.put("memberExpireTime", afterUser.getMemberExpireTime() != null ? 
                    afterUser.getMemberExpireTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                after.put("realName", afterUser.getRealName());
                after.put("phone", afterUser.getPhone());
                after.put("email", afterUser.getEmail());
                
                detail.put("before", before);
                detail.put("after", after);
                
                log.setModifyDetail(objectMapper.writeValueAsString(detail));
            } catch (Exception e) {
                // JSON序列化失败，不记录详情
                System.err.println("记录修改详情失败：" + e.getMessage());
            }
            
            log.setCreateTime(LocalDateTime.now());
            userModifyLogMapper.insert(log);
        }
    }
    
    /**
     * 获取会员类型文本
     */
    private String getMemberTypeText(Integer memberType) {
        if (memberType == null || memberType == 0) {
            return "普通用户";
        } else if (memberType == 1) {
            return "VIP";
        } else if (memberType == 2) {
            return "SVIP";
        }
        return "未知";
    }
    
    /**
     * 查询最近的修改日志（分页），支持按被修改用户ID或用户名模糊查询
     */
    public PageResult<UserModifyLog> getRecentLogs(Integer page, Integer pageSize, String keyword) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<UserModifyLog> list = userModifyLogMapper.selectRecentLogs(offset, pageSize, keyword);
        long total = userModifyLogMapper.countAll(keyword);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 根据ID查询日志详情
     */
    public UserModifyLog getLogById(Long id) {
        return userModifyLogMapper.selectById(id);
    }
}

