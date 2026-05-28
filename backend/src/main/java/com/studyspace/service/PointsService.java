package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.Points;
import com.studyspace.entity.User;
import com.studyspace.mapper.PointsMapper;
import com.studyspace.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 积分服务类
 */
@Service
public class PointsService {
    
    @Autowired
    private PointsMapper pointsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 增加积分并记录流水
     */
    @Transactional
    public void addPoints(Long userId, Integer points, String type, String description, Long relatedId) {
        if (points <= 0) {
            throw new RuntimeException("积分数量必须大于0");
        }
        
        // 更新用户总积分
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        int newTotalPoints = (user.getTotalPoints() == null ? 0 : user.getTotalPoints()) + points;
        user.setTotalPoints(newTotalPoints);
        userMapper.update(user);
        
        // 记录积分流水
        Points pointsRecord = new Points();
        pointsRecord.setUserId(userId);
        pointsRecord.setPoints(points);
        pointsRecord.setType(type);
        pointsRecord.setDescription(description);
        pointsRecord.setRelatedId(relatedId);
        pointsMapper.insert(pointsRecord);
    }
    
    /**
     * 扣除积分并记录流水
     */
    @Transactional
    public void deductPoints(Long userId, Integer points, String type, String description, Long relatedId) {
        if (points <= 0) {
            throw new RuntimeException("积分数量必须大于0");
        }
        
        // 检查用户积分是否足够
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        int currentPoints = user.getTotalPoints() == null ? 0 : user.getTotalPoints();
        if (currentPoints < points) {
            throw new RuntimeException("积分不足，当前积分：" + currentPoints + "，需要积分：" + points);
        }
        
        // 更新用户总积分
        int newTotalPoints = currentPoints - points;
        user.setTotalPoints(newTotalPoints);
        userMapper.update(user);
        
        // 记录积分流水
        Points pointsRecord = new Points();
        pointsRecord.setUserId(userId);
        pointsRecord.setPoints(-points); // 负数表示扣除
        pointsRecord.setType(type);
        pointsRecord.setDescription(description);
        pointsRecord.setRelatedId(relatedId);
        pointsMapper.insert(pointsRecord);
    }
    
    /**
     * 分页查询用户积分流水记录
     */
    public PageResult<Points> getPointsHistory(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<Points> list = pointsMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        long total = pointsMapper.countByUserId(userId);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 获取用户当前总积分
     */
    public Integer getUserTotalPoints(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getTotalPoints() == null ? 0 : user.getTotalPoints();
    }
    
    /**
     * 检查用户今日是否已签到
     */
    public boolean checkDailySignIn(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Points signIn = pointsMapper.selectTodaySignIn(userId, today);
        return signIn != null;
    }
    
    /**
     * 执行每日签到，增加1积分
     */
    @Transactional
    public void dailySignIn(Long userId) {
        // 检查今日是否已签到
        if (checkDailySignIn(userId)) {
            throw new RuntimeException("今日已签到，请明天再来");
        }
        
        // 增加1积分
        addPoints(userId, 1, "签到", "每日签到奖励", null);
    }
    
    /**
     * 根据预约时长计算应得积分（每2小时+1积分，取整）
     */
    public Integer calculateReservationPoints(Integer hours) {
        if (hours == null || hours <= 0) {
            return 0;
        }
        // 向下取整：每2小时+1积分
        return hours / 2;
    }
    
    /**
     * 在预约座位状态变为"使用中"时自动赠送积分
     */
    @Transactional
    public void awardReservationPoints(Long userId, Long reservationId, Integer hours) {
        if (hours == null || hours <= 0) {
            return;
        }
        
        Integer points = calculateReservationPoints(hours);
        if (points > 0) {
            addPoints(userId, points, "预约奖励", 
                     String.format("预约%d小时，获得%d积分", hours, points), 
                     reservationId);
        }
    }
    
    /**
     * 判断用户积分是否足够兑换指定时长（30积分=2小时）
     */
    public boolean canExchangeWithPoints(Integer hours, Integer totalPoints) {
        if (hours == null || hours <= 0) {
            return false;
        }
        if (totalPoints == null) {
            totalPoints = 0;
        }
        
        // 兑换规则：30积分=2小时，所需积分 = (时长/2) * 30
        int requiredPoints = (hours / 2) * 30;
        
        // 检查是否以30的倍数兑换
        if (hours % 2 != 0) {
            return false; // 仅支持以2小时的倍数兑换
        }
        
        return totalPoints >= requiredPoints;
    }
    
    /**
     * 使用积分兑换预约，扣除相应积分并返回所需积分
     * @param userId 用户ID
     * @param twoHourMultiples 2小时的倍数（例如：1表示2小时，2表示4小时）
     * @param relatedId 关联的预约ID
     * @return 扣除的积分数
     */
    @Transactional
    public Integer exchangeWithPoints(Long userId, Integer twoHourMultiples, Long relatedId) {
        if (twoHourMultiples == null || twoHourMultiples <= 0) {
            throw new RuntimeException("预约时长必须大于0");
        }
        
        // 计算所需积分：30积分 * 2小时的倍数
        int requiredPoints = twoHourMultiples * 30;
        
        // 检查用户积分是否足够
        Integer currentPoints = getUserTotalPoints(userId);
        if (currentPoints < requiredPoints) {
            throw new RuntimeException(String.format("积分不足，当前积分：%d，需要积分：%d", currentPoints, requiredPoints));
        }
        
        // 扣除积分
        int actualHours = twoHourMultiples * 2; // 实际小时数
        deductPoints(userId, requiredPoints, "积分兑换", 
                    String.format("使用%d积分兑换%d小时学习时长", requiredPoints, actualHours), 
                    relatedId);
        
        return requiredPoints;
    }
    
    /**
     * 分页查询所有积分流水记录（管理员使用）
     */
    public PageResult<Points> getAllPointsHistory(Integer page, Integer pageSize, String username) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<Points> list = pointsMapper.selectAllWithPagination(offset, pageSize, username);
        long total = pointsMapper.countAll(username);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 管理员手动调整用户积分
     */
    @Transactional
    public void adjustPoints(Long userId, Integer points, String description) {
        if (points == 0) {
            throw new RuntimeException("调整积分不能为0");
        }
        
        if (points > 0) {
            addPoints(userId, points, "管理员调整", description, null);
        } else {
            deductPoints(userId, -points, "管理员调整", description, null);
        }
    }
    
    /**
     * 检查用户今日是否已获得长期租赁积分奖励
     */
    public boolean hasReceivedLeaseRewardToday(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Points reward = pointsMapper.selectTodayLeaseReward(userId, today);
        return reward != null;
    }
    
    /**
     * 赠送长期租赁积分奖励（每日15积分）
     */
    @Transactional
    public void awardLeaseReward(Long userId, Long leaseId) {
        // 检查今日是否已赠送
        if (hasReceivedLeaseRewardToday(userId)) {
            return; // 今日已赠送，跳过
        }
        
        // 赠送15积分
        addPoints(userId, 15, "长期租赁奖励", "长期租赁每日奖励", leaseId);
    }
}

