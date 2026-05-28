package com.studyspace.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 留言板数据传输对象
 */
@Data
public class MessageBoardDTO {
    
    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000个字符")
    private String content;
    
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
}

