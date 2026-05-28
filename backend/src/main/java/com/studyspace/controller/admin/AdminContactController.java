package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.AdminContact;
import com.studyspace.service.AdminContactService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员联系方式管理控制器
 */
@RestController
@RequestMapping("/admin/contact")
public class AdminContactController extends BaseController {
    
    @Autowired
    private AdminContactService adminContactService;
    
    /**
     * 获取联系方式（管理员）
     */
    @GetMapping("/info")
    public Result<AdminContact> getContactInfo(@RequestHeader("Authorization") String token) {
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
            
            AdminContact contact = adminContactService.getContact();
            return success(contact);
        } catch (Exception e) {
            return error("获取联系方式失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新联系方式（管理员）
     */
    @PutMapping("/update")
    public Result<AdminContact> updateContact(@RequestHeader("Authorization") String token,
                                              @RequestBody AdminContact adminContact) {
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
            
            adminContactService.updateContact(adminContact);
            AdminContact updated = adminContactService.getContact();
            return success("更新成功", updated);
        } catch (Exception e) {
            return error("更新联系方式失败：" + e.getMessage());
        }
    }
}

