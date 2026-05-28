package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.MemberOrder;
import com.studyspace.service.AlipayService;
import com.studyspace.service.MemberService;
import com.studyspace.config.AlipayConfig;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员控制器
 */
@RestController
@RequestMapping("/member")
public class MemberController extends BaseController {
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private AlipayService alipayService;
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    /**
     * 创建会员订单
     */
    @PostMapping("/create")
    public Result<MemberOrder> createMemberOrder(@RequestHeader("Authorization") String token,
                                                 @RequestParam("memberType") Integer memberType) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            MemberOrder order = memberService.createMemberOrder(userId, memberType);
            return success("订单创建成功，请完成支付", order);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("创建会员订单失败：" + e.getMessage());
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
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            // 获取订单信息
            MemberOrder order = memberService.getMemberOrderById(id);
            if (order == null) {
                return error("订单不存在");
            }
            
            // 检查是否为该用户的订单
            if (!order.getUserId().equals(userId)) {
                return error("无权支付该订单");
            }
            
            // 检查订单状态
            if (order.getPaymentStatus() == 1) {
                return error("该订单已支付");
            }
            
            // 创建支付订单
            String outTradeNo = order.getOrderNumber();
            String memberTypeName = order.getMemberType() == 1 ? "VIP" : "SVIP";
            String subject = "会员购买-" + memberTypeName + "-" + outTradeNo;
            String body = String.format("购买%s会员，有效期：%s 至 %s", 
                memberTypeName, order.getStartDate(), order.getEndDate());
            
            String paymentForm = alipayService.createPayment(
                outTradeNo,
                order.getAmount(),
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
    public String alipayNotify(@RequestParam Map<String, String> params) {
        try {
            System.out.println("会员订单支付宝异步回调参数：" + params);
            
            // 验证签名
            boolean signVerified = alipayService.verifySignature(params);
            if (!signVerified) {
                System.err.println("会员订单支付宝异步回调签名验证失败");
                return "fail";
            }
            
            // 获取订单信息
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");
            
            System.out.println("会员订单号：" + outTradeNo + "，交易状态：" + tradeStatus + "，支付宝交易号：" + tradeNo);
            
            // 处理支付成功的订单
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                try {
                    if (outTradeNo != null && outTradeNo.startsWith("MEMBER")) {
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
            System.err.println("处理会员订单支付宝异步回调异常：" + e.getMessage());
            e.printStackTrace();
            return "fail";
        }
    }
    
    /**
     * 支付宝支付同步回调（用户支付完成后跳转）
     */
    @GetMapping("/pay/return")
    public String alipayReturn(@RequestParam Map<String, String> params) {
        try {
            System.out.println("会员订单支付宝同步回调参数：" + params);
            
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");
            
            System.out.println("会员订单号：" + outTradeNo + "，交易状态：" + tradeStatus + "，支付宝交易号：" + tradeNo);
            
            // 验证签名
            boolean signVerified = alipayService.verifySignature(params);
            if (!signVerified) {
                System.err.println("会员订单支付宝同步回调签名验证失败，但继续处理（用户已支付）");
            } else {
                System.out.println("会员订单支付宝同步回调签名验证成功");
            }
            
            // 判断支付成功
            boolean isPaymentSuccess = false;
            if (outTradeNo != null && tradeNo != null) {
                if (tradeStatus != null) {
                    isPaymentSuccess = "TRADE_SUCCESS".equals(tradeStatus) 
                                    || "TRADE_FINISHED".equals(tradeStatus) 
                                    || "TRADE_HAS_SUCCESS".equals(tradeStatus);
                } else {
                    isPaymentSuccess = true;
                    System.out.println("同步回调中无 trade_status，但存在 trade_no，判断为支付成功");
                }
            }
            
            if (isPaymentSuccess) {
                try {
                    if (outTradeNo != null && outTradeNo.startsWith("MEMBER")) {
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
            
            // 回跳到会员中心页面
            String frontendBaseUrl = params.get("frontBase");
            if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
                frontendBaseUrl = alipayConfig.getFrontendBaseUrl();
            }
            if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
                frontendBaseUrl = "http://localhost:3000";
            }
            String finalRedirect = frontendBaseUrl + "/user/member?paySuccess=true";
            
            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付成功</title></head>" +
                   "<body><script>window.location.href='" + finalRedirect + "';</script>" +
                   "<p>支付成功，正在跳转...</p></body></html>";
        } catch (Exception e) {
            System.err.println("处理会员订单支付宝同步回调异常：" + e.getMessage());
            e.printStackTrace();
            return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>支付失败</title></head>" +
                   "<body><p>支付处理失败，请联系客服</p></body></html>";
        }
    }
    
    /**
     * 查询当前用户的会员订单列表
     */
    @GetMapping("/my-orders")
    public Result<List<MemberOrder>> getMyMemberOrders(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            List<MemberOrder> orders = memberService.getMemberOrdersByUserId(userId);
            return success(orders);
        } catch (Exception e) {
            return error("查询会员订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询会员订单详情
     */
    @GetMapping("/order/{id}")
    public Result<MemberOrder> getMemberOrderById(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            MemberOrder order = memberService.getMemberOrderById(id);
            if (order == null) {
                return error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId)) {
                return error("无权查看该订单");
            }
            
            return success(order);
        } catch (Exception e) {
            return error("查询订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 用户主动取消未支付会员订单
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancelMemberOrder(@RequestHeader("Authorization") String token,
                                            @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error("无效的token");
            }

            memberService.cancelMemberOrder(id, userId);
            return success("取消成功");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("取消失败：" + e.getMessage());
        }
    }
}

