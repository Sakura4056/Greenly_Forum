package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.ReminderDTO;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.ReminderConfig;

public interface ReminderService extends IService<Reminder> {
    ReminderConfig updateConfig(ReminderDTO.ConfigUpdateRequest request, Long userId);
    ReminderConfig getConfig(Long userId);
    ReminderDTO.UnreadResponse getUnread(Long userId);
    void markAsRead(Long reminderId, Long userId);
    Integer getUnreadCount(Long userId);
    void markAllAsRead(Long userId);
}
