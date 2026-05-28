package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.dto.AnnouncementDTO;
import com.studyspace.entity.Announcement;
import com.studyspace.service.AnnouncementService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员公告管理控制器
 */
@RestController
@RequestMapping("/admin/announcement")
public class AdminAnnouncementController extends BaseController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 查询所有公告（管理员使用）
     */
    @GetMapping("/list")
    public Result<List<Announcement>> getAllAnnouncements(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return success(announcements);
        } catch (Exception e) {
            return error("查询公告列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布公告
     */
    @PostMapping("/create")
    public Result<Announcement> createAnnouncement(@RequestHeader("Authorization") String token,
                                                    @Validated @RequestBody AnnouncementDTO announcementDTO) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long publisherId = JwtUtil.getUserIdFromToken(actualToken);
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            Announcement announcement = new Announcement();
            BeanUtils.copyProperties(announcementDTO, announcement);
            announcement.setPublisherId(publisherId);
            announcement.setStatus(1); // 发布中
            
            announcementService.createAnnouncement(announcement);
            return success("公告发布成功", announcement);
        } catch (Exception e) {
            return error("发布公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新公告
     */
    @PutMapping("/update/{id}")
    public Result<Announcement> updateAnnouncement(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long id,
                                                    @Validated @RequestBody AnnouncementDTO announcementDTO) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            Announcement announcement = announcementService.getAnnouncementById(id);
            if (announcement == null) {
                return error("公告不存在");
            }
            
            BeanUtils.copyProperties(announcementDTO, announcement);
            announcementService.updateAnnouncement(announcement);
            
            Announcement updated = announcementService.getAnnouncementById(id);
            return success("公告更新成功", updated);
        } catch (Exception e) {
            return error("更新公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除公告（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAnnouncement(@RequestHeader("Authorization") String token,
                                           @PathVariable Long id) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            announcementService.deleteAnnouncement(id);
            return success("公告删除成功", null);
        } catch (Exception e) {
            return error("删除公告失败：" + e.getMessage());
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

