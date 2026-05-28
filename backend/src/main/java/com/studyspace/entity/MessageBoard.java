package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 留言板实体类
 */
@Data
public class MessageBoard implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 留言ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 留言内容
     */
    private String content;
    
    /**
     * 是否匿名：0-不匿名，1-匿名
     */
    private Integer isAnonymous;
    
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
    
    /**
     * 用户头像URL（仅非匿名时返回）
     */
    private String avatarUrl;
    
    /**
     * 评论数量（不映射到数据库，通过查询计算）
     */
    private Integer commentCount;
    
    /**
     * 点赞数量（不映射到数据库，通过查询计算）
     */
    private Integer likeCount;
    
    /**
     * 当前用户是否已点赞（不映射到数据库，通过查询判断）
     */
    private Boolean isLiked;
}

