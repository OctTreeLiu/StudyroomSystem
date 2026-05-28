package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 使用记录实体类
 */
@Data
public class UsageRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 自习室ID
     */
    private Long roomId;
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 预约ID（如果是预约使用）
     */
    private Long reservationId;
    
    /**
     * 租赁ID（如果是租赁使用）
     */
    private Long leaseId;
    
    /**
     * 开始使用时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束使用时间
     */
    private LocalDateTime endTime;
    
    /**
     * 使用时长（分钟）
     */
    private Integer duration;
    
    /**
     * 使用类型：1-预约使用，2-长期租赁使用
     */
    private Integer type;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    // 关联字段（不映射到数据库）
    private String username;
    private String roomName;
    private String roomNumber;
    private String seatNumber;
    private String seatName;
}

