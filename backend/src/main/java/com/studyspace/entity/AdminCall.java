package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员呼叫记录实体类
 */
@Data
public class AdminCall implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 呼叫ID
     */
    private Long id;
    
    /**
     * 呼叫用户ID
     */
    private Long userId;
    
    /**
     * 呼叫用户名
     */
    private String username;
    
    /**
     * 用户联系电话
     */
    private String phone;
    
    /**
     * 座位信息
     */
    private String seatInfo;
    
    /**
     * 用户留言
     */
    private String message;
    
    /**
     * 处理状态：0-待处理，1-已处理
     */
    private Integer status;
    
    /**
     * 呼叫时间
     */
    private LocalDateTime createTime;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理管理员ID
     */
    private Long handleAdminId;
    
    // 关联字段（不映射到数据库）
    private String handleAdminName;
}

