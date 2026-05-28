package com.studyspace.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 大模型对话消息DTO
 */
@Data
public class AiChatMessageDTO {

    @NotBlank(message = "role不能为空")
    private String role; // system / user / assistant

    @NotBlank(message = "content不能为空")
    private String content;
}

