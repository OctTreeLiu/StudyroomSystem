package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.StudyRoom;
import com.studyspace.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自习室控制器
 */
@RestController
@RequestMapping("/studyroom")
public class StudyRoomController extends BaseController {
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    /**
     * 查询所有自习室（普通用户和管理员都可以访问）
     */
    @GetMapping("/list")
    public Result<List<StudyRoom>> getAllStudyRooms() {
        try {
            List<StudyRoom> studyRooms = studyRoomService.getAllStudyRooms();
            return success(studyRooms);
        } catch (Exception e) {
            return error("查询自习室列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询自习室详情
     */
    @GetMapping("/{id}")
    public Result<StudyRoom> getStudyRoomById(@PathVariable Long id) {
        try {
            StudyRoom studyRoom = studyRoomService.getStudyRoomById(id);
            if (studyRoom == null) {
                return error("自习室不存在");
            }
            return success(studyRoom);
        } catch (Exception e) {
            return error("查询自习室详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询自习室
     */
    @GetMapping("/list/status/{status}")
    public Result<List<StudyRoom>> getStudyRoomsByStatus(@PathVariable Integer status) {
        try {
            List<StudyRoom> studyRooms = studyRoomService.getStudyRoomsByStatus(status);
            return success(studyRooms);
        } catch (Exception e) {
            return error("查询自习室列表失败：" + e.getMessage());
        }
    }
}

