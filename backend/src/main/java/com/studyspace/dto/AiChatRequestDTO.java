package com.studyspace.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 大模型对话请求DTO
 */
@Data
public class AiChatRequestDTO {

    @NotEmpty(message = "messages不能为空")
    private List<AiChatMessageDTO> messages;
}

