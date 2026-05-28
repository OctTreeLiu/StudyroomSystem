package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自习室实体类
 */
@Data
public class StudyRoom implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 自习室ID
     */
    private Long id;
    
    /**
     * 自习室编号
     */
    private String roomNumber;
    
    /**
     * 自习室名称
     */
    private String roomName;
    
    /**
     * 容量（座位数）
     */
    private Integer capacity;
    
    /**
     * 位置描述
     */
    private String location;
    
    /**
     * 状态：0-有座（有可用座位），1-无座（无可用座位）
     */
    private Integer status;
    
    /**
     * 自习室描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

