package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 用户ID（NULL表示发送给所有管理员）
     */
    private Long userId;
    
    /**
     * 消息类型：1-预约提醒，2-系统通知，3-管理员通知
     */
    private Integer type;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 关联ID（如预约ID）
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    // 关联字段（不映射到数据库）
    private String username;
}

