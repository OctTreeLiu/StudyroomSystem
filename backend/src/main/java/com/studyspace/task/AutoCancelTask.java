package com.studyspace.task;

import com.studyspace.service.LongTermLeaseService;
import com.studyspace.service.ReservationService;
import com.studyspace.service.ExtraChargeOrderService;
import com.studyspace.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动取消超时未付款的预约/长期租赁/额外收费/会员订单任务
 */
@Component
public class AutoCancelTask {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private LongTermLeaseService longTermLeaseService;
    
    @Autowired
    private ExtraChargeOrderService extraChargeOrderService;
    
    @Autowired
    private MemberService memberService;

    /**
     * 每分钟执行：取消超过5分钟未支付的预约
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutReservations() {
        reservationService.cancelTimeoutUnpaidReservations();
    }

    /**
     * 每分钟执行：取消审核通过但超时未支付的长期租赁
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutLeases() {
        int canceledCount = longTermLeaseService.cancelExpiredUnpaidLeases();
        if (canceledCount > 0) {
            System.out.println("自动取消长期租赁订单: " + canceledCount + " 个订单已取消");
        }
    }
    
    /**
     * 每小时执行：取消超过24小时未支付的额外收费订单
     */
    @Scheduled(fixedRate = 3600000)
    public void cancelExpiredExtraChargeOrders() {
        extraChargeOrderService.cancelExpiredUnpaidOrders();
    }
    
    /**
     * 每分钟执行：取消超过5分钟未支付的会员订单
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutMemberOrders() {
        int canceledCount = memberService.cancelTimeoutUnpaidOrders(5);
        if (canceledCount > 0) {
            System.out.println("自动取消会员订单: " + canceledCount + " 个订单已取消");
        }
    }
}


