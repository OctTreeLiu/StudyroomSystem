package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 长期租赁实体类
 */
@Data
public class LongTermLease implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租赁ID
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
     * 租赁编号
     */
    private String leaseNumber;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 状态：0-待审核，1-审核通过待付款，2-已付款生效（待使用/未开始），3-已拒绝，4-已过期，5-使用中，6-使用结束，7-已退款
     */
    private Integer status;
    
    /**
     * 付款状态：0-未付款，1-已付款
     */
    private Integer paymentStatus;
    
    /**
     * 付款截止时间（审核通过后24小时内）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 付款时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 退款金额
     */

    private BigDecimal refundAmount;


    /**
     * 退款时间
     */

    private LocalDateTime refundTime;

    /**
     * 退款状态: 0-未退款, 1-退款成功, 2-退款失败
     */
    private Integer refundStatus;

    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
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
    private String roomName;
    private String roomNumber;
    private String seatNumber;
    private String seatName;
}

