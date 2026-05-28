package com.studyspace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象（不包含密码）
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private String gender;
    private Integer age;
    private String hobby;
    private String university;
    private String signature;
    private Integer totalPoints;
    private Integer memberType;
    private LocalDateTime memberExpireTime;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

