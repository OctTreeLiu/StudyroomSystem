package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约实体类
 */
@Data
public class Reservation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 预约ID
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
     * 预约编号
     */
    private String reservationNumber;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：0-待付款，1-已付款待使用，2-使用中，3-已完成，4-已取消
     */
    private Integer status;
    
    /**
     * 付款状态：0-未付款，1-已付款
     */
    private Integer paymentStatus;
    
    /**
     * 付款时间
     */
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    private java.math.BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private java.time.LocalDateTime refundTime;

    /**
     * 退款状态: 0-未退款, 1-退款成功, 2-退款失败
     */
    private Integer refundStatus;

    /**
     * 退款原因
     */
    private String refundReason;

    
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

