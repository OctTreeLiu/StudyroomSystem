package com.studyspace.service;

import com.studyspace.entity.AdminContact;
import com.studyspace.mapper.AdminContactMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理员联系方式服务类
 */
@Service
public class AdminContactService {
    
    @Autowired
    private AdminContactMapper adminContactMapper;
    
    /**
     * 获取联系方式
     */
    public AdminContact getContact() {
        AdminContact contact = adminContactMapper.selectFirst();
        // 如果不存在，创建默认记录
        if (contact == null) {
            contact = new AdminContact();
            contact.setPhone("12345678901");
            contact.setEmail("1234567890@qq.com");
            adminContactMapper.insert(contact);
            return adminContactMapper.selectFirst();
        }
        return contact;
    }
    
    /**
     * 更新联系方式
     */
    public void updateContact(AdminContact adminContact) {
        AdminContact existing = adminContactMapper.selectFirst();
        if (existing != null) {
            adminContact.setId(existing.getId());
            adminContactMapper.update(adminContact);
        } else {
            adminContactMapper.insert(adminContact);
        }
    }
}

