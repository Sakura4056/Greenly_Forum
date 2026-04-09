package com.plant.backend.service;

import java.util.List;
import com.plant.backend.entity.CareSchedule;

public interface NotificationService {
    void sendCareReminderSummary(Long userId, List<CareSchedule> dueSchedules);
}
