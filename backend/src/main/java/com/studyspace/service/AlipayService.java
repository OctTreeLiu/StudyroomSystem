package com.studyspace.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.studyspace.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务
 */
@Service
public class AlipayService {
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    /**
     * 退款接口（带重试机制）
     */
    public boolean refund(String outTradeNo, String tradeNo, BigDecimal refundAmount, String refundReason) {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getAppPrivateKey(),
                "json",
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );

        final int maxAttempts = 3;
        // 第一次使用固定的 outRequestNo，利用支付宝的幂等性
        // 如果第一次失败，后续重试使用不同的 outRequestNo 避免幂等冲突
        String baseOutRequestNo = outTradeNo + "_REFUND";
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // 第一次使用固定值，后续重试加上时间戳确保唯一性
            String outRequestNo = attempt == 1 ? baseOutRequestNo : baseOutRequestNo + "_" + System.currentTimeMillis();
            try {
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                AlipayTradeRefundModel model = new AlipayTradeRefundModel();
                if (outTradeNo != null && !outTradeNo.isEmpty()) {
                    model.setOutTradeNo(outTradeNo);
                }
                if (tradeNo != null && !tradeNo.isEmpty()) {
                    model.setTradeNo(tradeNo);
                }
                model.setRefundAmount(refundAmount.toString());
                model.setRefundReason(refundReason);
                model.setOutRequestNo(outRequestNo);
                request.setBizModel(model);

                AlipayTradeRefundResponse response = alipayClient.execute(request);
                if (response.isSuccess()) {
                    System.out.println("支付宝退款成功，订单号:" + outTradeNo + ", 尝试次数:" + attempt);
                    return true;
                }

                String subCode = response.getSubCode();
                String subMsg = response.getSubMsg();
                String msg = "支付宝退款失败:" + subCode + ", " + subMsg;
                System.err.println(msg);
                System.err.println("退款请求参数: outTradeNo=" + outTradeNo
                        + ", tradeNo=" + (tradeNo == null ? "" : tradeNo)
                        + ", refundAmount=" + refundAmount
                        + ", outRequestNo=" + outRequestNo
                        + ", attempt=" + attempt + "/" + maxAttempts);

                // aop.ACQ.SYSTEM_ERROR 属于支付宝系统异常，短暂等待后重试
                if ("aop.ACQ.SYSTEM_ERROR".equals(subCode) && attempt < maxAttempts) {
                    try {
                        Thread.sleep(400L * attempt); // 递增延迟：400ms, 800ms
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // 其他错误码，如果不是最后一次尝试，也进行重试（可能是网络问题）
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(400L * attempt);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // 最后一次尝试也失败，返回 false
                return false;
            } catch (AlipayApiException e) {
                String msg = "调用支付宝退款接口异常:" + e.getMessage();
                System.err.println(msg);
                System.err.println("退款请求参数: outTradeNo=" + outTradeNo
                        + ", tradeNo=" + (tradeNo == null ? "" : tradeNo)
                        + ", refundAmount=" + refundAmount
                        + ", outRequestNo=" + outRequestNo
                        + ", attempt=" + attempt + "/" + maxAttempts);

                // 如果是最后一次尝试，直接返回 false
                if (attempt >= maxAttempts) {
                    return false;
                }

                // 网络异常或系统异常，等待后重试
                try {
                    Thread.sleep(400L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                String msg = "退款处理未知异常:" + e.getMessage();
                System.err.println(msg);
                e.printStackTrace();

                // 如果是最后一次尝试，直接返回 false
                if (attempt >= maxAttempts) {
                    return false;
                }

                // 其他异常，等待后重试
                try {
                    Thread.sleep(400L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return false;
    }

    /**
     * 退款接口（失败时抛出明确错误，便于前端展示）
     */
    public void refundOrThrow(String outTradeNo, String tradeNo, BigDecimal refundAmount, String refundReason) {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getAppPrivateKey(),
                "json",
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );

        final int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String outRequestNo = outTradeNo + "_" + System.currentTimeMillis();
            try {
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                AlipayTradeRefundModel model = new AlipayTradeRefundModel();
                if (outTradeNo != null && !outTradeNo.isEmpty()) {
                    model.setOutTradeNo(outTradeNo);
                }
                if (tradeNo != null && !tradeNo.isEmpty()) {
                    model.setTradeNo(tradeNo);
                }
                model.setRefundAmount(refundAmount.toString());
                model.setRefundReason(refundReason);
                model.setOutRequestNo(outRequestNo);
                request.setBizModel(model);

                AlipayTradeRefundResponse response = alipayClient.execute(request);
                if (response.isSuccess()) {
                    System.out.println("支付宝退款成功，订单号:" + outTradeNo);
                    return;
                }

                String subCode = response.getSubCode();
                String subMsg = response.getSubMsg();
                String msg = "支付宝退款失败:" + subCode + ", " + subMsg;
                System.err.println(msg);
                System.err.println("退款请求参数: outTradeNo=" + outTradeNo
                        + ", tradeNo=" + (tradeNo == null ? "" : tradeNo)
                        + ", refundAmount=" + refundAmount
                        + ", outRequestNo=" + outRequestNo
                        + ", attempt=" + attempt + "/" + maxAttempts);

                // aop.ACQ.SYSTEM_ERROR 属于支付宝系统异常，短暂等待后重试
                if ("aop.ACQ.SYSTEM_ERROR".equals(subCode) && attempt < maxAttempts) {
                    try {
                        Thread.sleep(400L * attempt);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                throw new RuntimeException(msg);
            } catch (AlipayApiException e) {
                String msg = "调用支付宝退款接口异常:" + e.getMessage();
                System.err.println(msg);
                System.err.println("退款请求参数: outTradeNo=" + outTradeNo
                        + ", tradeNo=" + (tradeNo == null ? "" : tradeNo)
                        + ", refundAmount=" + refundAmount
                        + ", outRequestNo=" + outRequestNo
                        + ", attempt=" + attempt + "/" + maxAttempts);

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(400L * attempt);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                throw new RuntimeException(msg);
            }
        }

        throw new RuntimeException("支付宝退款失败: 系统异常");
    }
    
    /**
     * 创建支付订单（电脑网站支付）
     * 
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单总金额
     * @param subject 订单标题
     * @param body 订单描述
     * @return 支付表单HTML
     */
    public String createPayment(String outTradeNo,
                               BigDecimal totalAmount,
                               String subject,
                               String body,
                               String frontBase) {
        try {
            System.out.println("开始创建支付订单，订单号：" + outTradeNo + "，金额：" + totalAmount);
            System.out.println("支付宝网关：" + alipayConfig.getGatewayUrl());
            System.out.println("应用ID：" + alipayConfig.getAppId());
            System.out.println("回调URL：" + alipayConfig.getReturnUrl());
            System.out.println("通知URL：" + alipayConfig.getNotifyUrl());
            
            // 获得初始化的AlipayClient
            AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getAppPrivateKey(),
                "json",
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
            );
            
            // 设置请求参数
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            
            // 对于沙箱环境，如果回调URL是localhost：
            // 1. return-url（同步回调）可以保留，因为是在用户浏览器中执行的
            // 2. notify-url（异步回调）需要移除，因为支付宝服务器无法访问localhost
            String returnUrl = alipayConfig.getReturnUrl();
            String notifyUrl = alipayConfig.getNotifyUrl();

            // 将前端实际访问的域名带回同步回调，避免回跳到错误的源导致 token 丢失
            if (returnUrl != null && frontBase != null && !frontBase.isEmpty()) {
                returnUrl = appendQuery(returnUrl, "frontBase", frontBase);
            }
            
            // 如果notify-url包含localhost，在沙箱环境中暂时不设置，避免支付宝报错
            if (notifyUrl != null && notifyUrl.contains("localhost")) {
                System.out.println("警告：异步回调URL包含localhost，支付宝服务器无法访问，暂时不设置notify-url");
                notifyUrl = null; // 不设置异步回调URL
            }
            
            // 设置同步回调URL（用户支付完成后跳转）
            if (returnUrl != null && !returnUrl.isEmpty()) {
                alipayRequest.setReturnUrl(returnUrl);
                System.out.println("设置同步回调URL：" + returnUrl);
            }
            
            // 设置异步回调URL（如果可用）
            if (notifyUrl != null && !notifyUrl.isEmpty()) {
                alipayRequest.setNotifyUrl(notifyUrl);
                System.out.println("设置异步回调URL：" + notifyUrl);
            } else {
                System.out.println("未设置异步回调URL（使用同步回调处理支付状态）");
            }
            
            // 设置业务参数
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount.toString());
            model.setSubject(subject);
            model.setBody(body);
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 固定值
            
            alipayRequest.setBizModel(model);
            
            System.out.println("支付参数设置完成，开始请求支付宝...");
            
            // 请求
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
            
            System.out.println("支付宝响应：" + response.getBody());
            
            if (response.isSuccess()) {
                System.out.println("支付表单生成成功");
                return response.getBody();
            } else {
                String errorMsg = "创建支付订单失败：" + response.getMsg() + "，错误码：" + response.getCode();
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (AlipayApiException e) {
            String errorMsg = "创建支付订单异常：" + e.getMessage() + "，错误码：" + e.getErrCode();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "创建支付订单未知异常：" + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * 验证支付宝回调签名
     * 
     * @param params 回调参数
     * @return 验证是否通过
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            return false;
        }
    }

    /**
     * 在 URL 上附加查询参数
     */
    private String appendQuery(String url, String name, String value) {
        try {
            if (url == null || url.isEmpty() || value == null || value.isEmpty()) {
                return url;
            }
            String delimiter = url.contains("?") ? "&" : "?";
            return url + delimiter + name + "=" + java.net.URLEncoder.encode(value, alipayConfig.getCharset());
        } catch (Exception e) {
            System.err.println("附加回跳参数失败：" + e.getMessage());
            return url;
        }
    }
}

