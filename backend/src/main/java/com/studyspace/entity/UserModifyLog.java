package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户修改日志实体类
 */
@Data
public class UserModifyLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 被修改的用户ID
     */
    private Long userId;
    
    /**
     * 操作的管理员ID
     */
    private Long adminId;
    
    /**
     * 操作的管理员用户名
     */
    private String adminUsername;
    
    /**
     * 被修改的用户名
     */
    private String userUsername;
    
    /**
     * 修改前的会员类型：0-普通用户，1-VIP，2-SVIP
     */
    private Integer beforeMemberType;
    
    /**
     * 修改后的会员类型：0-普通用户，1-VIP，2-SVIP
     */
    private Integer afterMemberType;
    
    /**
     * 修改前的会员到期时间
     */
    private LocalDateTime beforeMemberExpireTime;
    
    /**
     * 修改后的会员到期时间
     */
    private LocalDateTime afterMemberExpireTime;
    
    /**
     * 修改详情（JSON格式，存储修改前后的完整信息）
     */
    private String modifyDetail;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

