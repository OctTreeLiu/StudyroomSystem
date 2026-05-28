package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 座位实体类
 */
@Data
public class Seat implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 座位ID
     */
    private Long id;
    
    /**
     * 所属自习室ID
     */
    private Long roomId;
    
    /**
     * 座位编号（如：A01, A02等）
     */
    private String seatNumber;
    
    /**
     * 座位名称
     */
    private String seatName;
    
    /**
     * 状态：0-空闲，1-已被预约，2-被长期租赁，3-维护中，4-已锁定（未支付状态）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 关联字段（不映射到数据库）
    private String roomName;
    private String roomNumber;
}

