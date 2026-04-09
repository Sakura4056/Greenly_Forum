package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.AnnouncementDTO;
import com.plant.backend.entity.Announcement;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.User;
import com.plant.backend.mapper.AnnouncementMapper;
import com.plant.backend.mapper.ReminderMapper;
import com.plant.backend.service.AnnouncementService;
import com.plant.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 * 
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final UserService userService;
    private final ReminderMapper reminderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Announcement publishAnnouncement(Long publisherId, String title, String content) {
        log.info("开始发布公告，标题：{}，发布人 ID：{}", title, publisherId);
        
        // 1. 创建公告记录
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublisherId(publisherId);
        announcement.setStatus(1); // 已发布
        announcement.setPublishTime(LocalDateTime.now());
        
        save(announcement);
        log.info("公告记录创建成功，公告 ID：{}", announcement.getId());
        
        // 2. 为所有用户创建提醒通知
        List<User> allUsers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)); // 只给活跃用户发送
        
        if (allUsers != null && !allUsers.isEmpty()) {
            List<Reminder> reminders = allUsers.stream()
                    .map(user -> {
                        Reminder reminder = new Reminder();
                        reminder.setUserId(user.getUserId());
                        reminder.setScene("announcement");
                        reminder.setBusinessId(announcement.getId());
                        reminder.setTitle("新公告：" + title);
                        reminder.setContent(content);
                        reminder.setIsRead(0); // 未读
                        return reminder;
                    })
                    .collect(Collectors.toList());
            
            // 批量插入提醒
            saveBatchReminders(reminders);
            log.info("公告发布成功，已为 {} 个用户创建提醒", reminders.size());
        } else {
            log.warn("系统中没有活跃用户，跳过提醒创建");
        }
        
        return announcement;
    }

    @Override
    public Page<AnnouncementDTO.AnnouncementResponse> listAnnouncements(Page<Announcement> page) {
        log.debug("查询公告列表，页码：{}，每页数量：{}", page.getCurrent(), page.getSize());
        
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getPublishTime);
        
        Page<Announcement> result = page(page, wrapper);
        
        // 转换为 DTO
        Page<AnnouncementDTO.AnnouncementResponse> dtoPage = new Page<>();
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        dtoPage.setTotal(result.getTotal());
        
        List<AnnouncementDTO.AnnouncementResponse> dtos = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    /**
     * 批量保存提醒（分批处理以避免单次插入过多数据）
     */
    private void saveBatchReminders(List<Reminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return;
        }
        
        int batchSize = 500; // 每批 500 条
        for (int i = 0; i < reminders.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, reminders.size());
            List<Reminder> batch = reminders.subList(i, endIndex);
            
            // 使用 MyBatis-Plus 的批量插入
            for (Reminder reminder : batch) {
                reminderMapper.insert(reminder);
            }
            log.debug("已插入 {} 条提醒记录", batch.size());
        }
    }

    /**
     * 将 Announcement 实体转换为 AnnouncementResponse DTO
     */
    private AnnouncementDTO.AnnouncementResponse convertToResponse(Announcement announcement) {
        AnnouncementDTO.AnnouncementResponse response = new AnnouncementDTO.AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setPublisherId(announcement.getPublisherId());
        response.setPublishTime(announcement.getPublishTime());
        response.setStatus(announcement.getStatus());
        return response;
    }
}
