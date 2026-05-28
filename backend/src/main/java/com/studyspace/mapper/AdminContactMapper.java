package com.studyspace.mapper;

import com.studyspace.entity.AdminContact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员联系方式Mapper接口
 */
@Mapper
public interface AdminContactMapper {
    
    /**
     * 查询联系方式（只取第一条记录）
     */
    AdminContact selectFirst();
    
    /**
     * 更新联系方式
     */
    int update(AdminContact adminContact);
    
    /**
     * 插入联系方式
     */
    int insert(AdminContact adminContact);
}

