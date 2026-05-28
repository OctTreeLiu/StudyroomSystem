package com.studyspace.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {
    
    private static final String SECRET = "studyroom_secret_key_2024"; // 实际应用中应该从配置文件读取
    private static final long EXPIRE_TIME = 365L * 24 * 60 * 60 * 1000; // 365天（几乎不过期）
    
    /**
     * 生成token
     */
    public static String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
    
    /**
     * 从token中获取Claims（不检查过期时间）
     */
    public static Claims getClaimsFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            // 使用setAllowedClockSkewSeconds允许时钟偏差，并设置不检查过期
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .setAllowedClockSkewSeconds(Long.MAX_VALUE / 1000) // 允许最大时钟偏差
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 即使过期也返回Claims，不检查过期时间
            return e.getClaims();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // Token格式错误
            System.err.println("Token格式错误: " + e.getMessage());
            return null;
        } catch (io.jsonwebtoken.SignatureException e) {
            // Token签名验证失败
            System.err.println("Token签名验证失败: " + e.getMessage());
            return null;
        } catch (Exception e) {
            // 其他错误返回null
            System.err.println("Token解析失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        }
        return null;
    }
    
    /**
     * 从token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 从token中获取角色
     */
    public static Integer getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object role = claims.get("role");
            if (role instanceof Integer) {
                return (Integer) role;
            }
        }
        return null;
    }
    
    /**
     * 验证token是否有效（不检查过期时间）
     */
    public static boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null; // 不检查过期时间，只要token能解析就认为有效
    }
}

