package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 */
@Data
public class CommentLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 点赞ID
     */
    private Long id;
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

