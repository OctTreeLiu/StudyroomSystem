package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.Announcement;
import com.studyspace.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务类
 */
@Service
public class AnnouncementService {
    
    @Autowired
    private AnnouncementMapper announcementMapper;
    
    /**
     * 查询所有已发布的公告（普通用户使用）
     */
    public List<Announcement> getPublishedAnnouncements() {
        return announcementMapper.selectAllPublished();
    }
    
    /**
     * 分页查询所有已发布的公告（游客可见）
     */
    public PageResult<Announcement> getPublishedAnnouncementsWithPagination(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<Announcement> list = announcementMapper.selectAllPublishedWithPagination(offset, pageSize);
        long total = announcementMapper.countAllPublished();
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 查询所有公告（管理员使用）
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.selectAll();
    }
    
    /**
     * 根据ID查询公告
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }
    
    /**
     * 发布公告
     */
    public void createAnnouncement(Announcement announcement) {
        if (announcement.getStatus() == null) {
            announcement.setStatus(1); // 默认发布
        }
        announcementMapper.insert(announcement);
    }
    
    /**
     * 更新公告
     */
    public void updateAnnouncement(Announcement announcement) {
        announcementMapper.update(announcement);
    }
    
    /**
     * 删除公告（逻辑删除）
     */
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }
}

