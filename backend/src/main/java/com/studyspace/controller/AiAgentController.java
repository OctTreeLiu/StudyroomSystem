package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.dto.AiChatRequestDTO;
import com.studyspace.service.AiAgentService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小光智能体（大模型对话）
 */
@RestController
@RequestMapping("/ai")
public class AiAgentController extends BaseController {

    @Autowired
    private AiAgentService aiAgentService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(
            @RequestHeader("Authorization") String token,
            @RequestBody AiChatRequestDTO request
    ) {
        try {
            String actualToken = token == null ? null : token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error("无效的token");
            }

            if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
                return error("messages不能为空");
            }

            String reply = aiAgentService.chat(request.getMessages());

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            return success("对话成功", data);
        } catch (Exception e) {
            return error("AI对话失败：" + e.getMessage());
        }
    }
}

