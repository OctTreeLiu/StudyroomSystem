package com.studyspace.mapper;

import com.studyspace.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告Mapper接口
 */
@Mapper
public interface AnnouncementMapper {
    
    /**
     * 根据ID查询公告
     */
    Announcement selectById(@Param("id") Long id);
    
    /**
     * 查询所有公告（状态为1-发布中）
     */
    List<Announcement> selectAllPublished();
    
    /**
     * 查询所有公告（管理员使用）
     */
    List<Announcement> selectAll();
    
    /**
     * 分页查询所有已发布的公告
     */
    List<Announcement> selectAllPublishedWithPagination(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 统计已发布的公告总数
     */
    int countAllPublished();
    
    /**
     * 插入公告
     */
    int insert(Announcement announcement);
    
    /**
     * 更新公告
     */
    int update(Announcement announcement);
    
    /**
     * 删除公告（逻辑删除）
     */
    int deleteById(@Param("id") Long id);
}

