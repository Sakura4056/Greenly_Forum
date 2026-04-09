package com.plant.backend.base;

import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller 基类
 * 提供通用的方法和工具
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
public abstract class BaseController {

    /**
     * 返回成功结果
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 返回成功结果（无数据）
     */
    protected Result<Void> success() {
        return Result.success();
    }

    /**
     * 返回错误结果
     */
    protected <T> Result<T> error(String message) {
        return Result.error(message);
    }

    /**
     * 返回错误结果（带错误码）
     */
    protected <T> Result<T> error(int code, String message) {
        return Result.error(code, message);
    }

    /**
     * 从请求头提取 Token
     */
    protected String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        // 返回 null 而不是抛出异常，让调用者决定如何处理
        return null;
    }

    /**
     * 获取客户端 IP 地址
     */
    protected String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    /**
     * 记录操作日志
     */
    protected void logOperation(String operation, String detail) {
        log.info("Operation: {}, Detail: {}", operation, detail);
    }
}
