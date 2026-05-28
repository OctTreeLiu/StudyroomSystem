package com.studyspace.service;

import com.studyspace.entity.MemberOrder;
import com.studyspace.entity.User;
import com.studyspace.mapper.MemberOrderMapper;
import com.studyspace.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会员服务类
 */
@Service
public class MemberService {
    
    @Autowired
    private MemberOrderMapper memberOrderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private PriceConfigService priceConfigService;
    
    /**
     * 创建会员订单
     * @param userId 用户ID
     * @param memberType 会员类型：1-VIP，2-SVIP
     * @return 会员订单
     */
    @Transactional
    public MemberOrder createMemberOrder(Long userId, Integer memberType) {
        // 验证会员类型
        if (memberType == null || (memberType != 1 && memberType != 2)) {
            throw new RuntimeException("无效的会员类型");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 计算价格和积分（从配置读取）
        BigDecimal amount;
        Integer pointsAwarded;
        
        if (memberType == 1) {
            // VIP：从配置读取价格和积分
            amount = priceConfigService.getVipPrice();
            pointsAwarded = priceConfigService.getVipPoints();
        } else {
            // SVIP：从配置读取价格和积分
            amount = priceConfigService.getSvipPrice();
            pointsAwarded = priceConfigService.getSvipPoints();
        }
        
        // 计算会员期限（30个自然日）
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        
        // 生成订单编号
        String orderNumber = "MEMBER" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // 创建订单
        MemberOrder order = new MemberOrder();
        order.setUserId(userId);
        order.setOrderNumber(orderNumber);
        order.setMemberType(memberType);
        order.setAmount(amount);
        order.setPaymentStatus(0); // 未付款
        order.setStatus(0); // 待支付
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setPointsAwarded(pointsAwarded);
        
        memberOrderMapper.insert(order);
        
        return order;
    }
    
    /**
     * 支付会员订单
     */
    @Transactional
    public void payMemberOrder(Long orderId, Long userId) {
        MemberOrder order = memberOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权支付该订单");
        }
        
        if (order.getPaymentStatus() == 1) {
            throw new RuntimeException("订单已支付");
        }
        
        // 更新订单状态
        order.setPaymentStatus(1);
        order.setStatus(1); // 已支付
        order.setPaymentTime(LocalDateTime.now());
        memberOrderMapper.update(order);
        
        // 更新用户会员信息 - 采用叠加式有效期策略
        // 核心规则：每次购买会员，系统将当前会员剩余有效天数 + 30 天作为新的到期时间
        // 示例：当前会员剩余 10 天，用户再次购买会员，新会员有效期 = 10 + 30 = 40 天
        User user = userMapper.selectById(userId);
        if (user != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newExpireTime;
            
            // 判断用户是否已有会员（无论是否过期）
            boolean hasExistingMember = user.getMemberType() != null && user.getMemberType() > 0;
            
            if (hasExistingMember && user.getMemberExpireTime() != null) {
                // 用户已有会员，采用叠加式续费策略
                LocalDateTime expireTime = user.getMemberExpireTime();
                
                // 基于 max(当前时间, 到期时间) 计算新到期日
                // 如果会员未过期：从到期时间开始延长30天（实现剩余天数+30天的效果）
                // 如果会员已过期：从当前时间开始计算30天（剩余天数视为0）
                LocalDateTime baseTime = expireTime.isAfter(now) ? expireTime : now;
                
                // 叠加30天：新到期时间 = baseTime + 30天
                // 这样如果剩余10天，新到期时间 = (now + 10天) + 30天 = now + 40天，正好是剩余10天 + 30天
                newExpireTime = baseTime.plusDays(30);
            } else {
                // 用户没有会员或会员信息不完整，从当前时间开始计算30天
                newExpireTime = now.plusDays(30);
            }
            
            // 设置新的到期时间
            user.setMemberExpireTime(newExpireTime);
            
            // 更新会员类型（如果新购买的会员等级更高，则升级）
            if (user.getMemberType() == null || user.getMemberType() < order.getMemberType()) {
                user.setMemberType(order.getMemberType());
            }
            
            userMapper.update(user);
        }
        
        // 赠送积分
        pointsService.addPoints(userId, order.getPointsAwarded(), "会员赠送", 
                               String.format("购买%s会员，赠送%d积分", 
                                   order.getMemberType() == 1 ? "VIP" : "SVIP", 
                                   order.getPointsAwarded()), 
                               order.getId());
    }
    
    /**
     * 根据订单编号支付会员订单（用于支付回调）
     */
    @Transactional
    public void payMemberOrderByNumber(String orderNumber) {
        MemberOrder order = memberOrderMapper.selectByOrderNumber(orderNumber);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getPaymentStatus() == 1) {
            return; // 已支付，跳过
        }
        
        payMemberOrder(order.getId(), order.getUserId());
    }
    
    /**
     * 根据用户ID查询会员订单列表
     */
    public List<MemberOrder> getMemberOrdersByUserId(Long userId) {
        return memberOrderMapper.selectByUserId(userId);
    }
    
    /**
     * 根据订单ID查询会员订单
     */
    public MemberOrder getMemberOrderById(Long orderId) {
        return memberOrderMapper.selectById(orderId);
    }
    
    /**
     * 根据订单编号查询会员订单
     */
    public MemberOrder getMemberOrderByNumber(String orderNumber) {
        return memberOrderMapper.selectByOrderNumber(orderNumber);
    }
    
    /**
     * 查询所有会员订单（管理员使用）
     */
    public List<MemberOrder> getAllMemberOrders() {
        return memberOrderMapper.selectAll();
    }
    
    /**
     * 检查用户会员是否有效
     */
    public boolean isMemberValid(User user) {
        if (user == null) {
            return false;
        }
        
        if (user.getMemberType() == null || user.getMemberType() == 0) {
            return false;
        }
        
        if (user.getMemberExpireTime() == null) {
            return false;
        }
        
        return user.getMemberExpireTime().isAfter(LocalDateTime.now());
    }
    
    /**
     * 获取用户会员折扣（0.9表示9折，0.8表示8折，1.0表示无折扣）
     */
    public BigDecimal getMemberDiscount(User user) {
        if (!isMemberValid(user)) {
            return BigDecimal.ONE; // 无折扣
        }
        
        if (user.getMemberType() == 1) {
            // VIP：从配置读取折扣
            return priceConfigService.getVipDiscount();
        } else if (user.getMemberType() == 2) {
            // SVIP：从配置读取折扣
            return priceConfigService.getSvipDiscount();
        }
        
        return BigDecimal.ONE;
    }
    
    /**
     * 取消超过指定分钟数未支付的会员订单
     * @param timeoutMinutes 超时分钟数
     * @return 取消的订单数量
     */
    @Transactional
    public int cancelTimeoutUnpaidOrders(int timeoutMinutes) {
        // 查询超时未支付的订单（只查询状态为待支付的订单）
        List<MemberOrder> timeoutOrders = memberOrderMapper.selectTimeoutUnpaidOrders(timeoutMinutes);
        
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return 0;
        }
        
        int canceledCount = 0;
        for (MemberOrder order : timeoutOrders) {
            try {
                // 更新订单状态为已取消，保留订单记录
                order.setStatus(2); // 已取消
                order.setPaymentStatus(0); // 确保未付款状态
                memberOrderMapper.update(order);
                canceledCount++;
            } catch (Exception e) {
                // 记录错误但继续处理其他订单
                System.err.println("取消会员订单失败，订单ID: " + order.getId() + ", 错误: " + e.getMessage());
            }
        }
        
        return canceledCount;
    }

    /**
     * 用户主动取消未支付会员订单
     */
    @Transactional
    public void cancelMemberOrder(Long orderId, Long userId) {
        MemberOrder order = memberOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该订单");
        }

        // 已取消：幂等处理
        if (order.getStatus() != null && order.getStatus() == 2) {
            return;
        }
        // 已支付不允许取消
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
            throw new RuntimeException("该订单已支付，无法取消");
        }
        // 只有待支付订单允许取消
        if (order.getStatus() != null && order.getStatus() != 0) {
            throw new RuntimeException("订单状态不允许取消");
        }

        order.setStatus(2); // 已取消
        order.setPaymentStatus(0); // 确保未支付
        memberOrderMapper.update(order);
    }
}

