package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 备忘录实体类
 */
@Data
public class Memo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 备忘录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 主题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 日期
     */
    private LocalDate memoDate;
    
    /**
     * 状态：0-未处理，1-已处理
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
}

