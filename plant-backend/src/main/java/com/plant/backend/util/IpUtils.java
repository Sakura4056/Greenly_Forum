package com.plant.backend.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP 工具类
 * 提供 IP 地址获取和解析功能
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IP_6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取当前请求的客户端 IP
     */
    public static String getCurrentIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return LOCALHOST_IP;
        }
        return getClientIp(request);
    }

    /**
     * 从 Request 中获取客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isValidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isValidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        // 如果还是没有，使用 remoteAddr
        if (isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }

        // 处理 localhost
        if (LOCALHOST_IP_6.equals(ip)) {
            ip = LOCALHOST_IP;
        }

        return ip;
    }

    /**
     * 获取服务器本地 IP
     */
    public static String getLocalIp() {
        try {
            InetAddress inet = InetAddress.getLocalHost();
            String ip = inet.getHostAddress();
            if (inet.isLoopbackAddress()) {
                return LOCALHOST_IP;
            }
            return ip;
        } catch (UnknownHostException e) {
            return LOCALHOST_IP;
        }
    }

    /**
     * 判断 IP 是否有效（不为空且不是 unknown）
     */
    private static boolean isValidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 获取 HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 判断是否为内网 IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 处理 IPv6 localhost
        if (LOCALHOST_IP_6.equals(ip)) {
            return true;
        }
        
        // 处理 IPv4 localhost
        if (LOCALHOST_IP.equals(ip)) {
            return true;
        }
        
        // 检查是否为内网 IP 段
        return ip.startsWith("192.168.") || 
               ip.startsWith("10.") || 
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.2") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.");
    }
}
