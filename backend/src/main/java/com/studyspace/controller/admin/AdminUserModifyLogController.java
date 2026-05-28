package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.UserModifyLog;
import com.studyspace.service.UserModifyLogService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户修改日志控制器
 */
@RestController
@RequestMapping("/admin/user-modify-log")
public class AdminUserModifyLogController extends BaseController {
    
    @Autowired
    private UserModifyLogService userModifyLogService;
    
    /**
     * 查询修改日志列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<UserModifyLog>> getLogList(@RequestHeader("Authorization") String token,
                                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                                        @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) String keyword) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null || role != 1) {
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            PageResult<UserModifyLog> result = userModifyLogService.getRecentLogs(page, pageSize, keyword);
            return success(result);
        } catch (Exception e) {
            return error("查询修改日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询日志详情
     */
    @GetMapping("/{id}")
    public Result<UserModifyLog> getLogDetail(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null || role != 1) {
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            UserModifyLog log = userModifyLogService.getLogById(id);
            if (log == null) {
                return error("日志不存在");
            }
            return success(log);
        } catch (Exception e) {
            return error("查询日志详情失败：" + e.getMessage());
        }
    }
}

