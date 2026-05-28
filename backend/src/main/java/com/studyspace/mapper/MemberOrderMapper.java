package com.studyspace.mapper;

import com.studyspace.entity.MemberOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员订单Mapper接口
 */
@Mapper
public interface MemberOrderMapper {
    
    /**
     * 插入会员订单
     */
    int insert(MemberOrder memberOrder);
    
    /**
     * 根据ID查询会员订单
     */
    MemberOrder selectById(@Param("id") Long id);
    
    /**
     * 根据订单编号查询会员订单
     */
    MemberOrder selectByOrderNumber(@Param("orderNumber") String orderNumber);
    
    /**
     * 根据用户ID查询会员订单列表
     */
    List<MemberOrder> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新会员订单
     */
    int update(MemberOrder memberOrder);
    
    /**
     * 查询所有会员订单（管理员使用）
     */
    List<MemberOrder> selectAll();
    
    /**
     * 查询超过指定分钟数未支付的会员订单
     * @param minutes 超时分钟数
     * @return 超时未支付的订单列表
     */
    List<MemberOrder> selectTimeoutUnpaidOrders(@Param("minutes") int minutes);
}

