package com.plant.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

public class ReminderDTO {

    @Data
    public static class ConfigUpdateRequest {
        // 注意：邮箱地址统一从 sys_user 表获取，不再通过此接口更新
        private String phone;  // 用于未来短信通知扩展
        private Integer popupEnabled;
        private Integer bellEnabled;
        private String sceneConfig;
    }
    
    @Data
    public static class UnreadResponse {
        private Long totalUnread;
        // Map<Scene, List<Reminder>>
        private Map<String, List<Object>> details;
    }
}
