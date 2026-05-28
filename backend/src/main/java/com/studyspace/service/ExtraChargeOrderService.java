package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.ExtraChargeOrder;
import com.studyspace.mapper.ExtraChargeOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 额外收费订单服务类
 */
@Service
public class ExtraChargeOrderService {
    
    @Autowired
    private ExtraChargeOrderMapper extraChargeOrderMapper;
    
    /**
     * 创建额外收费订单
     */
    @Transactional
    public ExtraChargeOrder createOrder(Long userId, BigDecimal amount, String content) {
        ExtraChargeOrder order = new ExtraChargeOrder();
        order.setUserId(userId);
        order.setOrderNumber(generateOrderNumber());
        order.setAmount(amount);
        order.setContent(content);
        order.setPaymentStatus(0); // 未付款
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        extraChargeOrderMapper.insert(order);
        return order;
    }
    
    /**
     * 生成订单编号
     */
    private String generateOrderNumber() {
        return "EC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 根据ID查询订单
     */
    public ExtraChargeOrder getById(Long id) {
        return extraChargeOrderMapper.selectById(id);
    }
    
    /**
     * 根据订单编号查询订单
     */
    public ExtraChargeOrder getByOrderNumber(String orderNumber) {
        return extraChargeOrderMapper.selectByOrderNumber(orderNumber);
    }
    
    /**
     * 根据用户ID分页查询订单
     */
    public PageResult<ExtraChargeOrder> getOrdersByUserId(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<ExtraChargeOrder> list = extraChargeOrderMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        long total = extraChargeOrderMapper.countByUserId(userId);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 查询所有订单（管理员使用，分页）
     */
    public PageResult<ExtraChargeOrder> getAllOrders(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<ExtraChargeOrder> list = extraChargeOrderMapper.selectAllWithPagination(offset, pageSize);
        long total = extraChargeOrderMapper.countAll();
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 更新支付状态
     */
    @Transactional
    public void updatePaymentStatus(Long orderId, Integer paymentStatus) {
        ExtraChargeOrder order = extraChargeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        order.setPaymentStatus(paymentStatus);
        if (paymentStatus == 1) {
            order.setPaymentTime(LocalDateTime.now());
        }
        extraChargeOrderMapper.update(order);
    }
    
    /**
     * 根据订单编号更新支付状态
     */
    @Transactional
    public void updatePaymentStatusByOrderNumber(String orderNumber, Integer paymentStatus) {
        ExtraChargeOrder order = extraChargeOrderMapper.selectByOrderNumber(orderNumber);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查是否已支付，避免重复处理
        if (order.getPaymentStatus() == 1) {
            return;
        }
        
        order.setPaymentStatus(paymentStatus);
        if (paymentStatus == 1) {
            order.setPaymentTime(LocalDateTime.now());
        }
        extraChargeOrderMapper.update(order);
    }
    
    /**
     * 取消订单（24小时未支付）
     */
    @Transactional
    public void cancelExpiredUnpaidOrders() {
        List<ExtraChargeOrder> expiredOrders = extraChargeOrderMapper.selectExpiredUnpaidOrders();
        for (ExtraChargeOrder order : expiredOrders) {
            order.setPaymentStatus(2); // 已取消
            extraChargeOrderMapper.update(order);
        }
    }
    
    /**
     * 统计未支付订单数量（管理员）
     */
    public long countUnpaidOrders() {
        return extraChargeOrderMapper.countUnpaidOrders();
    }
    
    /**
     * 统计用户未支付订单数量
     */
    public long countUnpaidOrdersByUserId(Long userId) {
        return extraChargeOrderMapper.countUnpaidOrdersByUserId(userId);
    }

    /**
     * 用户主动取消未支付额外收费订单
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        ExtraChargeOrder order = extraChargeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该订单");
        }

        // 已取消：幂等
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 2) {
            return;
        }
        // 已支付不允许取消
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
            throw new RuntimeException("该订单已支付，无法取消");
        }
        // 仅未支付允许取消
        if (order.getPaymentStatus() != null && order.getPaymentStatus() != 0) {
            throw new RuntimeException("订单状态不允许取消");
        }

        order.setPaymentStatus(2);
        order.setPaymentTime(null);
        extraChargeOrderMapper.update(order);
    }
}

