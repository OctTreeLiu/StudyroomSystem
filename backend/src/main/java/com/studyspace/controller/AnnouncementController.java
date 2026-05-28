package com.studyspace.controller;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.entity.Announcement;
import com.studyspace.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController extends BaseController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 查询所有已发布的公告（普通用户使用，无需鉴权）
     */
    @GetMapping("/list")
    public Result<List<Announcement>> getPublishedAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getPublishedAnnouncements();
            return success(announcements);
        } catch (Exception e) {
            return error("查询公告列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询所有已发布的公告（游客可见，无需鉴权）
     */
    @GetMapping("/list/page")
    public Result<PageResult<Announcement>> getPublishedAnnouncementsWithPagination(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            PageResult<Announcement> pageResult = announcementService.getPublishedAnnouncementsWithPagination(page, pageSize);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询公告列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        try {
            Announcement announcement = announcementService.getAnnouncementById(id);
            if (announcement == null) {
                return error("公告不存在");
            }
            return success(announcement);
        } catch (Exception e) {
            return error("查询公告详情失败：" + e.getMessage());
        }
    }
}

