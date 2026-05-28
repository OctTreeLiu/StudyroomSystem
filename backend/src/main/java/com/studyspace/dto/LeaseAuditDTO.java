package com.studyspace.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 长期租赁审核DTO
 */
@Data
public class LeaseAuditDTO {
    
    /**
     * 审核结果：true-通过，false-拒绝
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    /**
     * 审核备注
     */
    private String auditRemark;
}

