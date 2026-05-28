package com.studyspace.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL（Base64或URL）
     */
    private String avatarUrl;
    
    /**
     * 性别：男/女
     */
    private String gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 兴趣爱好
     */
    private String hobby;
    
    /**
     * 在读/毕业高校
     */
    private String university;
    
    /**
     * 个性签名
     */
    private String signature;
    
    /**
     * 用户总积分
     */
    private Integer totalPoints;
    
    /**
     * 会员类型：0-普通用户，1-VIP，2-SVIP
     */
    private Integer memberType;
    
    /**
     * 会员到期时间
     */
    private LocalDateTime memberExpireTime;
    
    /**
     * 角色：0-普通用户，1-管理员
     */
    private Integer role;
    
    /**
     * 状态：0-禁用，1-启用
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

