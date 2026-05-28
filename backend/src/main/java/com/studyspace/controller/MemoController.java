package com.studyspace.controller;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.entity.Memo;
import com.studyspace.service.MemoService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 备忘录控制器
 */
@RestController
@RequestMapping("/memo")
public class MemoController extends BaseController {
    
    @Autowired
    private MemoService memoService;
    
    /**
     * 创建备忘录
     */
    @PostMapping("/create")
    public Result<Memo> createMemo(@RequestHeader("Authorization") String token,
                                    @RequestBody Memo memo) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            if (memo.getTitle() == null || memo.getTitle().trim().isEmpty()) {
                return error("主题不能为空");
            }
            
            if (memo.getMemoDate() == null) {
                return error("日期不能为空");
            }
            
            Memo createdMemo = memoService.createMemo(userId, memo);
            return success("创建成功", createdMemo);
        } catch (Exception e) {
            return error("创建备忘录失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新备忘录
     */
    @PutMapping("/update/{id}")
    public Result<Void> updateMemo(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id,
                                    @RequestBody Memo memo) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            memoService.updateMemo(userId, id, memo);
            return success("更新成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新备忘录失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除备忘录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMemo(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            memoService.deleteMemo(userId, id);
            return success("删除成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除备忘录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取我的所有备忘录（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Memo>> getMyMemos(@RequestHeader("Authorization") String token,
                                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String date,
                                                @RequestParam(required = false) Integer status) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            LocalDate memoDate = null;
            if (date != null && !date.trim().isEmpty()) {
                memoDate = LocalDate.parse(date);
            }
            
            PageResult<Memo> result;
            if (memoDate != null || status != null) {
                // 有筛选条件，使用筛选查询
                result = memoService.getMemosWithFilters(userId, memoDate, status, page, pageSize);
            } else {
                // 无筛选条件，使用普通分页查询
                result = memoService.getMemosByUserIdWithPagination(userId, page, pageSize);
            }
            
            return success(result);
        } catch (Exception e) {
            return error("获取备忘录列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据日期获取备忘录
     */
    @GetMapping("/list-by-date")
    public Result<List<Memo>> getMemosByDate(@RequestHeader("Authorization") String token,
                                              @RequestParam String date) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            LocalDate memoDate = LocalDate.parse(date);
            List<Memo> memos = memoService.getMemosByUserIdAndDate(userId, memoDate);
            return success(memos);
        } catch (Exception e) {
            return error("获取备忘录列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查今天是否有未处理的备忘录（用于侧边栏高亮）
     */
    @GetMapping("/check-today-unprocessed")
    public Result<Map<String, Object>> checkTodayUnprocessed(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            LocalDate today = LocalDate.now();
            boolean hasUnprocessed = memoService.hasUnprocessedMemo(userId, today);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasUnprocessed", hasUnprocessed);
            result.put("date", today.toString());
            
            return success(result);
        } catch (Exception e) {
            return error("检查失败：" + e.getMessage());
        }
    }
}

