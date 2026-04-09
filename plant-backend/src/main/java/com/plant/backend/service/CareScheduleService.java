package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.CareScheduleDTO;
import com.plant.backend.entity.CareSchedule;
import java.util.List;
import java.util.Map;

public interface CareScheduleService extends IService<CareSchedule> {
    CareSchedule add(CareScheduleDTO.AddRequest request);

    CareSchedule updateSchedule(CareScheduleDTO.UpdateRequest request, Long userId);

    Page<CareSchedule> query(CareScheduleDTO.Query query, Long currentUserId, String currentRole);

    void deleteSchedule(Long scheduleId, Long userId);
    Map<String, List<CareSchedule>> getCalendar(Integer year, Integer month, Long userId);


}
