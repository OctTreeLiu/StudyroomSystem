package com.studyspace.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * 座位时间段VO
 */
@Data
public class SeatTimeSlot implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 开始时间（仅时间部分，如 08:00）
     */
    private String startTime;
    
    /**
     * 结束时间（仅时间部分，如 10:00）
     */
    private String endTime;
    
    /**
     * 状态类型：free-空闲，reserved-已预约，locked-已锁定，leased-长期租赁
     */
    private String statusType;
    
    /**
     * 状态描述
     */
    private String statusText;
    
    /**
     * 关联的预约ID（如果是预约）
     */
    private Long reservationId;
    
    /**
     * 关联的长期租赁ID（如果是长期租赁）
     */
    private Long leaseId;
    
    /**
     * 用户信息（预约或租赁的用户）
     */
    private String username;
}

