package com.studyspace.mapper;

import com.studyspace.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface NotificationMapper {
    
    /**
     * 根据ID查询消息
     */
    Notification selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询消息列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户未读消息数量
     */
    int countUnreadByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有管理员的消息（userId为NULL）
     */
    List<Notification> selectAdminNotifications();
    
    /**
     * 插入消息
     */
    int insert(Notification notification);
    
    /**
     * 更新消息（主要用于标记已读）
     */
    int update(Notification notification);
    
    /**
     * 标记消息为已读
     */
    int markAsRead(@Param("id") Long id, @Param("readTime") java.time.LocalDateTime readTime);
    
    /**
     * 批量标记消息为已读
     */
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readTime") java.time.LocalDateTime readTime);
}

