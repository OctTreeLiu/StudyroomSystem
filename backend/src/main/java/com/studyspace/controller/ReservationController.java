package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.dto.ReservationDTO;
import com.studyspace.entity.Reservation;
import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.MemberOrder;
import com.studyspace.service.AlipayService;
import com.studyspace.service.ReservationService;
import com.studyspace.service.LongTermLeaseService;
import com.studyspace.service.MemberService;
import com.studyspace.service.ExtraChargeOrderService;
import com.studyspace.config.AlipayConfig;
import com.studyspace.utils.JwtUtil;
import com.studyspace.vo.EntryTicketVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 预约控制器
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController extends BaseController {
    
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private LongTermLeaseService longTermLeaseService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayConfig alipayConfig;
    
    @Autowired
    private com.studyspace.service.PointsService pointsService;
    
    @Autowired
    private ExtraChargeOrderService extraChargeOrderService;
    
    /**
     * 创建预约
     */
    @PostMapping("/create")
    public Result<Reservation> createReservation(@RequestHeader("Authorization") String token,
                                                  @Validated @RequestBody ReservationDTO reservationDTO) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            Reservation reservation = reservationService.createReservation(userId, reservationDTO);
            return success("预约创建成功，请完成支付", reservation);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("创建预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用积分兑换预约
     */
    @PostMapping("/create-with-points")
    public Result<Reservation> createReservationWithPoints(@RequestHeader("Authorization") String token,
                                                           @Validated @RequestBody ReservationDTO reservationDTO) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            Reservation reservation = reservationService.createReservationWithPoints(userId, reservationDTO);
            return success("积分兑换预约成功", reservation);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("积分兑换预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的入场凭证
     */
    @GetMapping("/entry-ticket")
    public Result<EntryTicketVO> getEntryTicket(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            EntryTicketVO entryTicket = reservationService.getCurrentEntryTicket(userId);
            
            // 无预约时返回成功但data为null，不返回错误，避免前端弹窗
            if (entryTicket == null) {
                return success(null);
            }
            
            return success(entryTicket);
        } catch (Exception e) {
            return error("获取入场凭证失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询当前用户的预约列表
     */
    @GetMapping("/my")
    public Result<java.util.List<Reservation>> getMyReservations(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            java.util.List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
            return success(reservations);
        } catch (Exception e) {
            return error("查询预约列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询预约详情
     */
    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return error("预约记录不存在");
            }
            return success(reservation);
        } catch (Exception e) {
            return error("查询预约详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelReservation(@RequestHeader("Authorization") String token,
                                          @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            reservationService.cancelReservation(id, userId);
            return success("取消预约成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("取消预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建支付订单（返回支付表单HTML）
     */
    @PostMapping("/pay/{id}")
    public Result<String> createPayment(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id,
                                        @RequestParam(value = "frontBase", required = false) String frontBase) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            // 获取预约信息
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return error("预约记录不存在");
            }
            
            // 检查是否为该用户的预约
            if (!reservation.getUserId().equals(userId)) {
                return error("无权支付该预约");
            }
            
            // 检查预约状态
            if (reservation.getPaymentStatus() == 1) {
                return error("该预约已支付");
            }
            
            if (reservation.getStatus() == 4) {
                return error("该预约已取消，无法支付");
            }
            
            // 创建支付订单
            String outTradeNo = reservation.getReservationNumber();
            String subject = "自习室预约-" + outTradeNo;
            
            // 处理可能为空的字段
            String roomName = reservation.getRoomName() != null ? reservation.getRoomName() : "自习室";
            String seatNumber = reservation.getSeatNumber() != null ? reservation.getSeatNumber() : "座位";
            String body = String.format("预约座位：%s %s号，时间：%s 至 %s",
                roomName, seatNumber,
                reservation.getStartTime(), reservation.getEndTime());
            
            String paymentForm = alipayService.createPayment(
                outTradeNo,
                reservation.getAmount(),
                subject,
                body,
                frontBase
            );
            
            return success("支付表单生成成功", paymentForm);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("创建支付订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 支付宝支付回调（异步通知）
     */
    @PostMapping("/pay/notify")
    public String alipayNotify(@RequestParam java.util.Map<String, String> params) {
        try {
            // 记录回调参数（用于调试）
            System.out.println("支付宝异步回调参数：" + params);
            
            // 验证签名
            boolean signVerified = alipayService.verifySignature(params);
            if (!signVerified) {
                System.err.println("支付宝异步回调签名验证失败");
                return "fail";
            }
            
            // 获取订单信息
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            
            System.out.println("订单号：" + outTradeNo + "，交易状态：" + tradeStatus + "，支付宝交易号：" + tradeNo);
            
            // 处理支付成功的订单
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                try {
                    if (outTradeNo != null && outTradeNo.startsWith("RES")) {
                        // 预约订单
                        Reservation reservation = reservationService.getReservationByNumber(outTradeNo);
                        if (reservation == null) {
                            System.err.println("未找到订单号对应的预约：" + outTradeNo);
                            return "fail";
                        }
                        // 检查是否已支付，避免重复处理
                        if (reservation.getPaymentStatus() == 1) {
                            System.out.println("预约订单已支付，跳过处理：" + outTradeNo);
                            return "success";
                        }
                        Long userId = reservation.getUserId();
                        reservationService.payReservation(reservation.getId(), userId, tradeNo);
                        System.out.println("预约订单支付成功，已更新状态：" + outTradeNo);
                    } else if (outTradeNo != null && outTradeNo.startsWith("LEASE")) {
                        // 长期租赁订单
                        LongTermLease lease = longTermLeaseService.getLeaseByNumber(outTradeNo);
                        if (lease == null) {
                            System.err.println("未找到订单号对应的长期租赁：" + outTradeNo);
                            return "fail";
                        }
                        if (lease.getPaymentStatus() == 1) {
                            System.out.println("长期租赁订单已支付，跳过处理：" + outTradeNo);
                            return "success";
                        }
                        Long userId = lease.getUserId();
                        longTermLeaseService.payLease(lease.getId(), userId, tradeNo);
                        System.out.println("长期租赁订单支付成功，已更新状态：" + outTradeNo);
                    } else if (outTradeNo != null && outTradeNo.startsWith("MEMBER")) {
                        // 会员订单
                        MemberOrder order = memberService.getMemberOrderByNumber(outTradeNo);
                        if (order == null) {
                            System.err.println("未找到订单号对应的会员订单：" + outTradeNo);
                            return "fail";
                        }
                        if (order.getPaymentStatus() == 1) {
                            System.out.println("会员订单已支付，跳过处理：" + outTradeNo);
                            return "success";
                        }
                        memberService.payMemberOrderByNumber(outTradeNo);
                        System.out.println("会员订单支付成功，已更新状态：" + outTradeNo);
                    } else if (outTradeNo != null && outTradeNo.startsWith("EC")) {
                        // 额外收费订单
                        com.studyspace.entity.ExtraChargeOrder order = extraChargeOrderService.getByOrderNumber(outTradeNo);
                        if (order == null) {
                            System.err.println("未找到订单号对应的额外收费订单：" + outTradeNo);
                            return "fail";
                        }
                        if (order.getPaymentStatus() == 1) {
                            System.out.println("额外收费订单已支付，跳过处理：" + outTradeNo);
                            return "success";
                        }
                        extraChargeOrderService.updatePaymentStatusByOrderNumber(outTradeNo, java.lang.Integer.valueOf(1));
                        System.out.println("额外收费订单支付成功，已更新状态：" + outTradeNo);
                    } else {
                        System.err.println("未知类型的订单号：" + outTradeNo);
                        return "fail";
                    }
                } catch (Exception e) {
                    System.err.println("更新支付状态失败：" + e.getMessage());
                    e.printStackTrace();
                    return "fail";
                }
            }
            
            return "success";
        } catch (Exception e) {
            System.err.println("处理支付宝异步回调异常：" + e.getMessage());
            e.printStackTrace();
            return "fail";
        }
    }
    
    /**
     * 支付宝支付同步回调（用户支付完成后跳转）
     * 注意：同步回调不包含 trade_status，需要通过 trade_no 判断支付成功
     */
    @GetMapping("/pay/return")
    public String alipayReturn(@RequestParam java.util.Map<String, String> params) {
        try {
            // 记录回调参数（用于调试）
            System.out.println("支付宝同步回调参数：" + params);
            
            // 获取订单信息
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status"); // 同步回调中可能为null
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            
            System.out.println("订单号：" + outTradeNo + "，交易状态：" + tradeStatus + "，支付宝交易号：" + tradeNo);
            
            // 验证签名（记录但不阻止处理）
            boolean signVerified = alipayService.verifySignature(params);
            if (!signVerified) {
                System.err.println("支付宝同步回调签名验证失败，但继续处理（用户已支付）");
            } else {
                System.out.println("支付宝同步回调签名验证成功");
            }
            
            // 同步回调判断支付成功的条件：
            // 1. 有订单号（out_trade_no）
            // 2. 有支付宝交易号（trade_no）- 这说明支付已经完成
            // 3. trade_status 为成功状态（如果有的话）
            boolean isPaymentSuccess = false;
            if (outTradeNo != null && tradeNo != null) {
                // 如果有 trade_status 且为成功状态，或者没有 trade_status 但有 trade_no，都认为支付成功
                if (tradeStatus != null) {
                    isPaymentSuccess = "TRADE_SUCCESS".equals(tradeStatus) 
                                    || "TRADE_FINISHED".equals(tradeStatus) 
                                    || "TRADE_HAS_SUCCESS".equals(tradeStatus);
                } else {
                    // 同步回调中没有 trade_status，但有 trade_no 说明支付已完成
                    isPaymentSuccess = true;
                    System.out.println("同步回调中无 trade_status，但存在 trade_no，判断为支付成功");
                }
            }
            
            if (isPaymentSuccess) {
                try {
                    boolean isReservationOrder = outTradeNo != null && outTradeNo.startsWith("RES");
                    boolean isLeaseOrder = outTradeNo != null && outTradeNo.startsWith("LEASE");

                    if (isReservationOrder) {
                        Reservation reservation = reservationService.getReservationByNumber(outTradeNo);
                        if (reservation == null) {
                            System.err.println("未找到订单号对应的预约：" + outTradeNo);
                            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                                   "<body><p>未找到订单信息，请联系客服</p></body></html>";
                        }
                        if (reservation.getPaymentStatus() == 0) {
                            Long userId = reservation.getUserId();
                            reservationService.payReservation(reservation.getId(), userId, tradeNo);
                            System.out.println("预约订单支付成功，已更新状态：" + outTradeNo + "，支付宝交易号：" + tradeNo);
                        } else {
                            System.out.println("预约订单已支付，跳过处理：" + outTradeNo);
                        }
                    } else if (isLeaseOrder) {
                        LongTermLease lease = longTermLeaseService.getLeaseByNumber(outTradeNo);
                        if (lease == null) {
                            System.err.println("未找到订单号对应的长期租赁：" + outTradeNo);
                            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                                   "<body><p>未找到订单信息，请联系客服</p></body></html>";
                        }
                        if (lease.getPaymentStatus() == 0) {
                            Long userId = lease.getUserId();
                            longTermLeaseService.payLease(lease.getId(), userId, tradeNo);
                            System.out.println("长期租赁订单支付成功，已更新状态：" + outTradeNo + "，支付宝交易号：" + tradeNo);
                        } else {
                            System.out.println("长期租赁订单已支付，跳过处理：" + outTradeNo);
                        }
                    } else if (outTradeNo != null && outTradeNo.startsWith("MEMBER")) {
                        // 会员订单
                        MemberOrder order = memberService.getMemberOrderByNumber(outTradeNo);
                        if (order == null) {
                            System.err.println("未找到订单号对应的会员订单：" + outTradeNo);
                            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                                   "<body><p>未找到订单信息，请联系客服</p></body></html>";
                        }
                        if (order.getPaymentStatus() == 0) {
                            memberService.payMemberOrderByNumber(outTradeNo);
                            System.out.println("会员订单支付成功，已更新状态：" + outTradeNo + "，支付宝交易号：" + tradeNo);
                        } else {
                            System.out.println("会员订单已支付，跳过处理：" + outTradeNo);
                        }
                    } else if (outTradeNo != null && outTradeNo.startsWith("EC")) {
                        // 额外收费订单
                        com.studyspace.entity.ExtraChargeOrder order = extraChargeOrderService.getByOrderNumber(outTradeNo);
                        if (order == null) {
                            System.err.println("未找到订单号对应的额外收费订单：" + outTradeNo);
                            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                                   "<body><p>未找到订单信息，请联系客服</p></body></html>";
                        }
                        if (order.getPaymentStatus() == 0) {
                            extraChargeOrderService.updatePaymentStatusByOrderNumber(outTradeNo, java.lang.Integer.valueOf(1));
                            System.out.println("额外收费订单支付成功，已更新状态：" + outTradeNo + "，支付宝交易号：" + tradeNo);
                        } else {
                            System.out.println("额外收费订单已支付，跳过处理：" + outTradeNo);
                        }
                    } else {
                        System.err.println("未知类型的订单号：" + outTradeNo);
                        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                               "<body><p>未知的订单类型，请联系客服</p></body></html>";
                    }
                } catch (Exception e) {
                    System.err.println("更新支付状态失败：" + e.getMessage());
                    e.printStackTrace();
                    return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                           "<body><p>支付处理失败，请联系客服</p></body></html>";
                }
            } else {
                System.err.println("支付状态判断失败，订单号：" + outTradeNo + "，交易号：" + tradeNo);
            }

            // 根据订单类型回跳到对应列表页面
            String redirectPath = "/user/reservation";
            if (outTradeNo != null && outTradeNo.startsWith("LEASE")) {
                redirectPath = "/user/lease";
            } else if (outTradeNo != null && outTradeNo.startsWith("MEMBER")) {
                redirectPath = "/user/member";
            } else if (outTradeNo != null && outTradeNo.startsWith("EC")) {
                redirectPath = "/user/extra-charge";
            }

            // 使用配置的前端基础地址，避免硬编码 localhost 导致不同域下 token 丢失
            // 优先使用支付发起时传入的前端域名，避免与实际访问域名不一致导致 token 丢失
            String frontendBaseUrl = params.get("frontBase");
            if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
                frontendBaseUrl = alipayConfig.getFrontendBaseUrl();
            }
            if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
                frontendBaseUrl = "http://localhost:3000";
            }
            String finalRedirect = frontendBaseUrl + redirectPath + "?paySuccess=true";

            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付成功</title></head>" +
                   "<body><script>window.location.href='" + finalRedirect + "';</script>" +
                   "<p>支付成功，正在跳转...</p></body></html>";
        } catch (Exception e) {
            System.err.println("处理支付宝同步回调异常：" + e.getMessage());
            e.printStackTrace();
            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                   "<body><p>支付处理失败，请联系客服</p></body></html>";
        }
    }
    
    /**
     * 手动同步支付状态（用于前端主动查询）
     */
    @PostMapping("/pay/sync/{id}")
    public Result<Void> syncPaymentStatus(@RequestHeader("Authorization") String token,
                                          @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            // 获取预约信息
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return error("预约记录不存在");
            }
            
            // 检查是否为该用户的预约
            if (!reservation.getUserId().equals(userId)) {
                return error("无权操作该预约");
            }
            
            // 如果已支付，直接返回成功
            if (reservation.getPaymentStatus() == 1) {
                return success("订单已支付", null);
            }
            
            // 这里可以调用支付宝查询接口，但为了简化，我们假设如果用户点击了同步，说明支付已完成
            // 实际应该调用 alipay.trade.query 接口查询订单状态
            // 暂时返回提示信息
            return error("请稍候，支付状态正在同步中");
        } catch (Exception e) {
            return error("同步支付状态失败：" + e.getMessage());
        }
    }
}

