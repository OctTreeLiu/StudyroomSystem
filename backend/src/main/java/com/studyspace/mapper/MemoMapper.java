package com.studyspace.mapper;

import com.studyspace.entity.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 备忘录Mapper接口
 */
@Mapper
public interface MemoMapper {
    
    /**
     * 根据ID查询备忘录
     */
    Memo selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询所有备忘录
     */
    List<Memo> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID分页查询备忘录
     */
    List<Memo> selectByUserIdWithPagination(@Param("userId") Long userId, 
                                            @Param("offset") int offset, 
                                            @Param("pageSize") int pageSize);
    
    /**
     * 统计用户备忘录总数
     */
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和日期查询备忘录
     */
    List<Memo> selectByUserIdAndDate(@Param("userId") Long userId, @Param("memoDate") LocalDate memoDate);
    
    /**
     * 根据用户ID和状态查询备忘录
     */
    List<Memo> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 根据用户ID、日期和状态分页查询备忘录
     */
    List<Memo> selectByUserIdWithFilters(@Param("userId") Long userId,
                                         @Param("memoDate") LocalDate memoDate,
                                         @Param("status") Integer status,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);
    
    /**
     * 统计符合条件的备忘录数量
     */
    long countByUserIdWithFilters(@Param("userId") Long userId,
                                  @Param("memoDate") LocalDate memoDate,
                                  @Param("status") Integer status);
    
    /**
     * 查询用户指定日期是否有未处理的备忘录
     */
    int countUnprocessedByUserIdAndDate(@Param("userId") Long userId, @Param("memoDate") LocalDate memoDate);
    
    /**
     * 插入备忘录
     */
    int insert(Memo memo);
    
    /**
     * 更新备忘录
     */
    int update(Memo memo);
    
    /**
     * 删除备忘录
     */
    int deleteById(@Param("id") Long id);
}

