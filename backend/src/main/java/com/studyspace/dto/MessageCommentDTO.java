package com.studyspace.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 留言评论数据传输对象
 */
@Data
public class MessageCommentDTO {
    
    @NotNull(message = "留言ID不能为空")
    private Long messageId;
    
    /**
     * 父评论ID（可选，用于创建次级评论）
     */
    private Long parentId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度在1到500个字符之间")
    private String content;
    
    private Boolean isAnonymous;
}

