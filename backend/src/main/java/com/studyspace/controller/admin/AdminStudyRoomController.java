package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.StudyRoom;
import com.studyspace.service.StudyRoomService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员自习室管理
 */
@RestController
@RequestMapping("/admin/studyroom")
public class AdminStudyRoomController extends BaseController {

    @Autowired
    private StudyRoomService studyRoomService;

    @PostMapping
    public Result<StudyRoom> createStudyRoom(@RequestHeader("Authorization") String token,
                                             @RequestBody StudyRoom body) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            if (role != 1) {
                return error(403, "无权限，仅管理员可操作");
            }
            StudyRoom created = studyRoomService.createStudyRoom(body);
            return success("新增自习室成功", created);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("新增自习室失败：" + e.getMessage());
        }
    }
}
