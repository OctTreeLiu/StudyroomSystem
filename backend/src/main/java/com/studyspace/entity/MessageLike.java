package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 留言点赞实体类
 */
@Data
public class MessageLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 点赞ID
     */
    private Long id;
    
    /**
     * 留言ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

