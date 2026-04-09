package com.plant.backend.util;

import lombok.Data;

/**
 * 统一响应结果封装
 * 
 * @param <T> 数据类型
 * @author Greenly Team
 * @date 2026-04-03
 */
@Data
public class Result<T> {
    /**
     * 响应码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private long timestamp;

    /**
     * 私有构造函数
     */
    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 错误响应（使用预定义错误码）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }
    
    /**
     * 错误响应（自定义错误码和消息）
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 错误响应（仅消息，使用默认业务错误码）
     */
    public static <T> Result<T> error(String msg) {
         return new Result<>(ResultCode.BUSINESS_ERROR.getCode(), msg, null);
    }

    /**
     * 错误响应（使用预定义错误码和自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String customMsg) {
        return new Result<>(resultCode.getCode(), customMsg, null);
    }

    /**
     * 参数错误响应
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(ResultCode.PARAM_ERROR.getCode(), msg, null);
    }

    /**
     * 未授权响应
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
    }

    /**
     * 未授权响应（自定义消息）
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), msg, null);
    }

    /**
     * 禁止访问响应
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMsg(), null);
    }

    /**
     * 资源不存在响应
     */
    public static <T> Result<T> notFound() {
        return new Result<>(ResultCode.NOT_FOUND.getCode(), ResultCode.NOT_FOUND.getMsg(), null);
    }

    /**
     * 资源不存在响应（自定义消息）
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(ResultCode.NOT_FOUND.getCode(), msg, null);
    }

    /**
     * 系统错误响应
     */
    public static <T> Result<T> serverError() {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), null);
    }

    /**
     * 系统错误响应（自定义消息）
     */
    public static <T> Result<T> serverError(String msg) {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), msg, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return this.code != ResultCode.SUCCESS.getCode();
    }
}
