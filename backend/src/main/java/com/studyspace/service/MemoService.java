package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.Memo;
import com.studyspace.mapper.MemoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 备忘录服务类
 */
@Service
public class MemoService {
    
    @Autowired
    private MemoMapper memoMapper;
    
    /**
     * 创建备忘录
     */
    @Transactional
    public Memo createMemo(Long userId, Memo memo) {
        memo.setUserId(userId);
        if (memo.getStatus() == null) {
            memo.setStatus(0); // 默认未处理
        }
        memoMapper.insert(memo);
        return memo;
    }
    
    /**
     * 更新备忘录
     */
    @Transactional
    public void updateMemo(Long userId, Long memoId, Memo memo) {
        Memo existingMemo = memoMapper.selectById(memoId);
        if (existingMemo == null) {
            throw new RuntimeException("备忘录不存在");
        }
        if (!existingMemo.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此备忘录");
        }
        
        memo.setId(memoId);
        memo.setUserId(userId);
        memoMapper.update(memo);
    }
    
    /**
     * 删除备忘录
     */
    @Transactional
    public void deleteMemo(Long userId, Long memoId) {
        Memo memo = memoMapper.selectById(memoId);
        if (memo == null) {
            throw new RuntimeException("备忘录不存在");
        }
        if (!memo.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此备忘录");
        }
        
        memoMapper.deleteById(memoId);
    }
    
    /**
     * 根据用户ID查询所有备忘录
     */
    public List<Memo> getMemosByUserId(Long userId) {
        return memoMapper.selectByUserId(userId);
    }
    
    /**
     * 根据用户ID分页查询备忘录
     */
    public PageResult<Memo> getMemosByUserIdWithPagination(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<Memo> list = memoMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        long total = memoMapper.countByUserId(userId);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 根据条件分页查询备忘录
     */
    public PageResult<Memo> getMemosWithFilters(Long userId, LocalDate memoDate, Integer status, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<Memo> list = memoMapper.selectByUserIdWithFilters(userId, memoDate, status, offset, pageSize);
        long total = memoMapper.countByUserIdWithFilters(userId, memoDate, status);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 根据用户ID和日期查询备忘录
     */
    public List<Memo> getMemosByUserIdAndDate(Long userId, LocalDate date) {
        return memoMapper.selectByUserIdAndDate(userId, date);
    }
    
    /**
     * 根据ID查询备忘录
     */
    public Memo getMemoById(Long id) {
        return memoMapper.selectById(id);
    }
    
    /**
     * 检查用户指定日期是否有未处理的备忘录
     */
    public boolean hasUnprocessedMemo(Long userId, LocalDate date) {
        int count = memoMapper.countUnprocessedByUserIdAndDate(userId, date);
        return count > 0;
    }
}

