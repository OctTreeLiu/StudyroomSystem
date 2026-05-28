package com.studyspace.controller;

import com.studyspace.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 基础控制器类
 */
public class BaseController {

    /**
     * 成功响应
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 成功响应（带消息）
     */
    protected <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    /**
     * 失败响应
     */
    protected <T> Result<T> error(String message) {
        return Result.error(message);
    }

    /**
     * 失败响应（带状态码）
     */
    protected <T> Result<T> error(Integer code, String message) {
        return Result.error(code, message);
    }

    /**
     * 未授权响应（401）
     */
    protected <T> ResponseEntity<Result<T>> unauthorized(String message) {
        Result<T> result = Result.error(401, message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}

