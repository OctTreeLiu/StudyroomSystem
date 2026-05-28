package com.studyspace.task;

import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.User;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.UserMapper;
import com.studyspace.service.PointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日长期租赁积分赠送任务
 * 每天00:00执行，为有长期租赁订单的用户赠送15积分
 */
@Component
public class DailyLeasePointsTask {

    private static final Logger logger = LoggerFactory.getLogger(DailyLeasePointsTask.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;

    @Autowired
    private PointsService pointsService;

    /**
     * 每天00:00执行：为有长期租赁订单的用户赠送15积分
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void awardLeasePoints() {
        logger.info("开始执行每日长期租赁积分赠送任务");
        
        try {
            // 查询所有普通用户（role = 0）
            List<User> users = userMapper.selectByRole(0);
            logger.info("查询到 {} 个普通用户", users.size());
            
            int successCount = 0;
            int skipCount = 0;
            
            for (User user : users) {
                try {
                    // 查询用户今天是否有有效的长期租赁订单
                    // 有效订单：status = 2（已付款生效），且今天在租赁期间内
                    LongTermLease activeLease = longTermLeaseMapper.selectCurrentActiveLeaseByUserId(user.getId());
                    
                    if (activeLease != null) {
                        // 检查今天是否已赠送过积分
                        if (!pointsService.hasReceivedLeaseRewardToday(user.getId())) {
                            // 赠送15积分
                            pointsService.awardLeaseReward(user.getId(), activeLease.getId());
                            successCount++;
                            logger.debug("用户 {} (ID: {}) 获得长期租赁积分奖励", user.getUsername(), user.getId());
                        } else {
                            skipCount++;
                            logger.debug("用户 {} (ID: {}) 今日已获得长期租赁积分奖励，跳过", user.getUsername(), user.getId());
                        }
                    }
                } catch (Exception e) {
                    logger.error("处理用户 {} (ID: {}) 的长期租赁积分奖励失败: {}", 
                            user.getUsername(), user.getId(), e.getMessage());
                }
            }
            
            logger.info("每日长期租赁积分赠送任务完成，成功赠送: {} 人，跳过: {} 人", successCount, skipCount);
        } catch (Exception e) {
            logger.error("执行每日长期租赁积分赠送任务失败: {}", e.getMessage(), e);
        }
    }
}

