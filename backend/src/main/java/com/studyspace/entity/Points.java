package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分流水记录实体类
 */
@Data
public class Points implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 积分记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分数量（正数表示增加，负数表示减少）
     */
    private Integer points;
    
    /**
     * 积分类型：签到/预约奖励/积分兑换/会员赠送/管理员调整
     */
    private String type;
    
    /**
     * 积分描述
     */
    private String description;
    
    /**
     * 关联ID（预约订单ID、会员订单ID等）
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    // 关联字段（不映射到数据库）
    private String username;
}

