package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.AdminContact;
import com.studyspace.service.AdminContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 联系方式控制器（公开接口）
 */
@RestController
@RequestMapping("/contact")
public class ContactController extends BaseController {
    
    @Autowired
    private AdminContactService adminContactService;
    
    /**
     * 获取管理员联系方式（公开接口，无需登录）
     */
    @GetMapping("/info")
    public Result<AdminContact> getContactInfo() {
        try {
            AdminContact contact = adminContactService.getContact();
            return success(contact);
        } catch (Exception e) {
            return error("获取联系方式失败：" + e.getMessage());
        }
    }
}

