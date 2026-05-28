package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.Reservation;
import com.studyspace.service.ReservationService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员预约管理控制器
 */
@RestController
@RequestMapping("/admin/reservation")
public class AdminReservationController extends BaseController {
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 查询所有预约记录（管理员使用）
     */
    @GetMapping("/list")
    public Result<List<Reservation>> getAllReservations(@RequestHeader("Authorization") String token,
                                                        @RequestParam(value = "date", required = false) String date) {
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
            
            List<Reservation> reservations;
            if (date != null && !date.trim().isEmpty()) {
                // 按日期查询
                reservations = reservationService.getAllByDateWithRuntimeStatus(date.trim());
            } else {
                // 查询所有
                reservations = reservationService.getAllWithRuntimeStatus();
            }
            return success(reservations);
        } catch (Exception e) {
            return error("查询预约列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询预约详情
     */
    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return error("预约记录不存在");
            }
            return success(reservation);
        } catch (Exception e) {
            return error("查询预约详情失败：" + e.getMessage());
        }
    }
}

