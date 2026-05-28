package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.Seat;
import com.studyspace.service.SeatService;
import com.studyspace.vo.SeatTimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位控制器
 */
@RestController
@RequestMapping("/seat")
public class SeatController extends BaseController {
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 根据自习室ID查询座位列表
     */
    @GetMapping("/list/room/{roomId}")
    public Result<List<Seat>> getSeatsByRoomId(@PathVariable Long roomId) {
        try {
            List<Seat> seats = seatService.getSeatsByRoomId(roomId);
            return success(seats);
        } catch (Exception e) {
            return error("查询座位列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询座位详情
     */
    @GetMapping("/{id}")
    public Result<Seat> getSeatById(@PathVariable Long id) {
        try {
            Seat seat = seatService.getSeatById(id);
            if (seat == null) {
                return error("座位不存在");
            }
            return success(seat);
        } catch (Exception e) {
            return error("查询座位详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据时间段和自习室ID查询可用座位
     */
    @GetMapping("/available")
    public Result<List<Seat>> getAvailableSeats(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            // 参数验证
            if (roomId == null) {
                return error("自习室ID不能为空");
            }
            if (startTime == null || endTime == null) {
                return error("开始时间和结束时间不能为空");
            }
            if (startTime.isAfter(endTime)) {
                return error("开始时间不能晚于结束时间");
            }
            
            List<Seat> seats = seatService.getAvailableSeatsByTimeRange(roomId, startTime, endTime);
            return success(seats);
        } catch (Exception e) {
            e.printStackTrace();
            return error("查询可用座位失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取座位使用情况时间轴（普通用户版本，不返回用户名）
     */
    @GetMapping("/{id}/timeline")
    public Result<List<SeatTimeSlot>> getSeatUsageTimeline(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            if (date == null) {
                return error("日期不能为空");
            }
            
            List<SeatTimeSlot> timeline = seatService.getSeatUsageTimelineForUser(id, date);
            return success(timeline);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("获取座位使用情况失败：" + e.getMessage());
        }
    }
}

