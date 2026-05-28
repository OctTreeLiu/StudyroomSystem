package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员订单实体类
 */
@Data
public class MemberOrder implements Serializable {
    
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
     * 会员类型：1-VIP，2-SVIP
     */
    private Integer memberType;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 付款状态：0-未付款，1-已付款
     */
    private Integer paymentStatus;
    
    /**
     * 订单状态：0-待支付，1-已支付，2-已取消
     */
    private Integer status;
    
    /**
     * 付款时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 会员开始日期
     */
    private LocalDate startDate;
    
    /**
     * 会员结束日期
     */
    private LocalDate endDate;
    
    /**
     * 赠送的积分数量
     */
    private Integer pointsAwarded;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 关联字段（不映射到数据库）
    private String username;
}

