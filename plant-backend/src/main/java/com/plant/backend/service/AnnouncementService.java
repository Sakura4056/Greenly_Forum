package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.AnnouncementDTO;
import com.plant.backend.entity.Announcement;

/**
 * 公告服务接口
 * 
 * @author Greenly Team
 * @date 2026-04-05
 */
public interface AnnouncementService extends IService<Announcement> {
    
    /**
     * 发布公告（同时为所有用户创建提醒）
     * 
     * @param publisherId 发布人 ID
     * @param title 公告标题
     * @param content 公告内容
     * @return 公告实体
     */
    Announcement publishAnnouncement(Long publisherId, String title, String content);

    /**
     * 分页查询公告列表
     * 
     * @param page 分页参数
     * @return 公告列表分页数据
     */
    Page<AnnouncementDTO.AnnouncementResponse> listAnnouncements(Page<Announcement> page);
}
