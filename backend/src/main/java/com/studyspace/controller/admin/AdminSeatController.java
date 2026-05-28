package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.Seat;
import com.studyspace.service.SeatService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理员座位管理控制器
 */
@RestController
@RequestMapping("/admin/seat")
public class AdminSeatController extends BaseController {
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 查询所有座位（管理员使用）
     */
    @GetMapping("/list")
    public Result<List<Seat>> getAllSeats(@RequestHeader("Authorization") String token) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            List<Seat> seats = seatService.getAllSeats();
            return success(seats);
        } catch (Exception e) {
            return error("查询座位列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据自习室ID查询座位列表（管理员使用）
     */
    @GetMapping("/list/room/{roomId}")
    public Result<List<Seat>> getSeatsByRoomId(@RequestHeader("Authorization") String token,
                                               @PathVariable Long roomId) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            List<Seat> seats = seatService.getSeatsByRoomId(roomId);
            return success(seats);
        } catch (Exception e) {
            return error("查询座位列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新座位状态（管理员使用）
     */
    @PutMapping("/update-status/{id}")
    public Result<Seat> updateSeatStatus(@RequestHeader("Authorization") String token,
                                         @PathVariable Long id,
                                         @RequestBody UpdateSeatStatusDTO dto) {
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
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            if (dto.getStatus() == null) {
                return error("状态不能为空");
            }
            
            // 验证状态值：0-空闲，1-已被预约，2-被长期租赁，3-维护中，4-已锁定（未支付）
            if (dto.getStatus() < 0 || dto.getStatus() > 4) {
                return error("无效的状态值");
            }
            
            Seat seat = seatService.getSeatById(id);
            if (seat == null) {
                return error("座位不存在");
            }

            // 只允许当前状态为“空闲(0)”或“维护中(3)”的座位进行状态修改
            Integer currentStatus = seat.getStatus();
            if (currentStatus == null || (currentStatus != 0 && currentStatus != 3)) {
                return error("仅当座位当前状态为“空闲”或“维护中”时才允许修改状态");
            }

            // 如果设置为“已锁定（未支付）”，必须存在未支付订单
            if (dto.getStatus() == 4) {
                boolean hasUnpaidOrders = seatService.hasUnpaidOrders(id);
                if (!hasUnpaidOrders) {
                    return error("当前座位没有未支付的预约或长期租赁订单，无法设置为锁定中");
                }
            }

            // 更新座位状态
            seat.setStatus(dto.getStatus());
            seatService.updateSeat(seat);
            
            // 重新查询更新后的座位信息
            Seat updatedSeat = seatService.getSeatById(id);
            return success("座位状态更新成功", updatedSeat);
        } catch (Exception e) {
            return error("更新座位状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询座位详情（管理员使用）
     */
    @GetMapping("/{id}")
    public Result<Seat> getSeatById(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
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
     * 获取座位使用情况时间轴
     */
    @GetMapping("/{id}/timeline")
    public Result<List<com.studyspace.vo.SeatTimeSlot>> getSeatUsageTimeline(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            if (date == null) {
                return error("日期不能为空");
            }
            
            List<com.studyspace.vo.SeatTimeSlot> timeline = seatService.getSeatUsageTimeline(id, date);
            return success(timeline);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("获取座位使用情况失败：" + e.getMessage());
        }
    }

    /**
     * 更新座位状态的DTO
     */
    public static class UpdateSeatStatusDTO {
        private Integer status;
        
        public Integer getStatus() {
            return status;
        }
        
        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}

