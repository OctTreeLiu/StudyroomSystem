package com.studyspace.mapper;

import com.studyspace.entity.ExtraChargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 额外收费订单Mapper接口
 */
@Mapper
public interface ExtraChargeOrderMapper {
    
    /**
     * 插入订单
     */
    int insert(ExtraChargeOrder order);
    
    /**
     * 根据ID查询订单
     */
    ExtraChargeOrder selectById(@Param("id") Long id);
    
    /**
     * 根据订单编号查询订单
     */
    ExtraChargeOrder selectByOrderNumber(@Param("orderNumber") String orderNumber);
    
    /**
     * 根据用户ID查询订单列表（分页）
     */
    List<ExtraChargeOrder> selectByUserIdWithPagination(@Param("userId") Long userId,
                                                         @Param("offset") int offset,
                                                         @Param("pageSize") int pageSize);
    
    /**
     * 统计用户订单总数
     */
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有订单（管理员使用，分页）
     */
    List<ExtraChargeOrder> selectAllWithPagination(@Param("offset") int offset,
                                                     @Param("pageSize") int pageSize);
    
    /**
     * 统计所有订单总数
     */
    long countAll();
    
    /**
     * 根据用户ID和支付状态查询订单列表（分页）
     */
    List<ExtraChargeOrder> selectByUserIdAndStatusWithPagination(@Param("userId") Long userId,
                                                                  @Param("paymentStatus") Integer paymentStatus,
                                                                  @Param("offset") int offset,
                                                                  @Param("pageSize") int pageSize);
    
    /**
     * 统计用户指定状态的订单数量
     */
    long countByUserIdAndStatus(@Param("userId") Long userId,
                                @Param("paymentStatus") Integer paymentStatus);
    
    /**
     * 更新订单
     */
    int update(ExtraChargeOrder order);
    
    /**
     * 统计未支付订单数量（用于菜单高亮）
     */
    long countUnpaidOrders();
    
    /**
     * 统计用户未支付订单数量（用于菜单高亮）
     */
    long countUnpaidOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * 查询超过24小时未支付的订单
     */
    List<ExtraChargeOrder> selectExpiredUnpaidOrders();
}

