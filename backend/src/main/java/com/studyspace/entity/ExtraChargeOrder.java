package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 额外收费订单实体类
 */
@Data
public class ExtraChargeOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单编号
     */
    private String orderNumber;
    
    /**
     * 收费金额
     */
    private BigDecimal amount;
    
    /**
     * 收费内容
     */
    private String content;
    
    /**
     * 付款状态：0-未付款，1-已付款，2-已取消
     */
    private Integer paymentStatus;
    
    /**
     * 付款时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 关联字段（不映射到数据库）
    /**
     * 用户名
     */
    private String username;
}

