package com.studyspace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 长期租赁DTO
 */
@Data
public class LongTermLeaseDTO {
    
    @NotNull(message = "自习室ID不能为空")
    private Long roomId;
    
    @NotNull(message = "座位ID不能为空")
    private Long seatId;
    
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;  // 前端传入开始日期的00:00:00
    
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;    // 前端传入结束日期的23:59:59
    
    @NotBlank(message = "申请理由不能为空")
    private String applyReason;
}

