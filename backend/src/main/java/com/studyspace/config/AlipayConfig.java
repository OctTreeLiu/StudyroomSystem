package com.studyspace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用私钥
     */
    private String appPrivateKey;
    
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    
    /**
     * 签名方式：RSA2
     */
    private String signType = "RSA2";
    
    /**
     * 字符编码格式
     */
    private String charset = "UTF-8";
    
    /**
     * 支付宝网关（沙箱环境）
     */
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";
    
    /**
     * 支付成功后跳转的页面
     */
    private String returnUrl;
    
    /**
     * 支付异步通知地址
     */
    private String notifyUrl;

    /**
     * 前端站点基础地址（用于支付完成后跳转回前端）
     */
    private String frontendBaseUrl = "http://localhost:3000";
}

