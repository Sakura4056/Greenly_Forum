package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.MyPlantDTO;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.entity.PlantPhoto;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.mapper.OfficialPlantMapper;
import com.plant.backend.service.CareRecordService;
import com.plant.backend.service.CareScheduleService;
import com.plant.backend.service.MyPlantService;
import com.plant.backend.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPlantServiceImpl extends ServiceImpl<MyPlantMapper, MyPlant> implements MyPlantService {
    
    private final CareScheduleService careScheduleService;
    private final CareRecordService careRecordService;
    private final PhotoService photoService;
    private final OfficialPlantMapper officialPlantMapper;

    @Override
    public Long addMyPlant(Long userId, MyPlantDTO.AddRequest request) {
        // 如果提供了 officialId，验证官方植物是否存在
        if (request.getOfficialId() != null) {
            OfficialPlant officialPlant = officialPlantMapper.selectById(request.getOfficialId());
            if (officialPlant == null) {
                log.warn("尝试关联不存在的官方植物ID: {}", request.getOfficialId());
                throw new BusinessException(400, "官方植物不存在");
            }
            log.info("成功关联官方植物: ID={}, Name={}", officialPlant.getId(), officialPlant.getName());
        }
        
        MyPlant plant = new MyPlant();
        BeanUtils.copyProperties(request, plant);
        if (StringUtils.isBlank(plant.getStatus())) {
            plant.setStatus("HEALTHY");
        }
        plant.setUserId(userId);
        this.save(plant);
        log.info("成功添加我的植物: plantId={}, userId={}, nickname={}", plant.getId(), userId, plant.getNickname());
        return plant.getId();
    }

    @Override
    public void updateMyPlant(Long userId, Long plantId, MyPlantDTO.UpdateRequest request) {
        MyPlant plant = this.getById(plantId);
        if (plant == null || !plant.getUserId().equals(userId)) {
            throw new BusinessException(404, "植物不存在");
        }
        
        // 如果提供了 officialId，验证官方植物是否存在
        if (request.getOfficialId() != null) {
            OfficialPlant officialPlant = officialPlantMapper.selectById(request.getOfficialId());
            if (officialPlant == null) {
                log.warn("尝试关联不存在的官方植物ID: {} (更新植物ID: {})", request.getOfficialId(), plantId);
                throw new BusinessException(400, "官方植物不存在");
            }
            log.info("成功关联官方植物: ID={}, Name={} (更新植物ID: {})", officialPlant.getId(), officialPlant.getName(), plantId);
        }
        
        BeanUtils.copyProperties(request, plant);
        this.updateById(plant);
        log.info("成功更新我的植物: plantId={}, userId={}, nickname={}", plantId, userId, plant.getNickname());
    }

    @Override
    public void deleteMyPlant(Long userId, Long plantId) {
        MyPlant plant = this.getById(plantId);
        if (plant == null || !plant.getUserId().equals(userId)) {
            throw new BusinessException(404, "植物不存在");
        }
        this.removeById(plantId);
    }

    @Override
    public Page<MyPlant> getMyPlants(Long userId, MyPlantDTO.QueryRequest query) {
        LambdaQueryWrapper<MyPlant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MyPlant::getUserId, userId);
        
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(MyPlant::getNickname, query.getKeyword());
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            wrapper.eq(MyPlant::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(MyPlant::getCreateTime);
        
        Page<MyPlant> page = new Page<>(query.getCurrent(), query.getSize());
        return this.page(page, wrapper);
    }

    @Override
    public MyPlant getMyPlantById(Long userId, Long plantId) {
        LambdaQueryWrapper<MyPlant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MyPlant::getId, plantId)
               .eq(MyPlant::getUserId, userId);
        MyPlant plant = this.getOne(wrapper);
        
        if (plant == null) {
            throw new BusinessException(404, "植物不存在或无权限访问");
        }
        
        return plant;
    }

    @Override
    public MyPlantDTO.DetailResponse getMyPlantDetail(Long userId, Long plantId) {
        MyPlant plant = this.getById(plantId);
        if (plant == null || !plant.getUserId().equals(userId)) {
            throw new BusinessException(404, "植物不存在");
        }

        MyPlantDTO.DetailResponse response = new MyPlantDTO.DetailResponse();
        response.setPlant(plant);

        // Fetch Recent 5 Care Schedules
        LambdaQueryWrapper<CareSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CareSchedule::getPlantId, plantId)
                       .eq(CareSchedule::getPlantSource, "LOCAL")
                       .orderByAsc(CareSchedule::getDueTime)
                       .last("LIMIT 5");
        List<CareSchedule> recentSchedules = careScheduleService.list(scheduleWrapper);
        response.setRecentSchedules(recentSchedules);
        
        // Fetch Recent 5 Records
        LambdaQueryWrapper<CareRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CareRecord::getPlantId, plantId)
                    .eq(CareRecord::getPlantSource, "LOCAL")
                    .orderByDesc(CareRecord::getRecordTime)
                    .last("LIMIT 5");
        List<CareRecord> recentRecords = careRecordService.list(recordWrapper);
        response.setRecentRecords(recentRecords);
        
        // Fetch Recent 5 Photos
        LambdaQueryWrapper<PlantPhoto> photoWrapper = new LambdaQueryWrapper<>();
        photoWrapper.eq(PlantPhoto::getPlantId, plantId)
                   .eq(PlantPhoto::getPlantSource, "LOCAL")
                   .orderByDesc(PlantPhoto::getCaptureTime)
                   .last("LIMIT 5");
        List<PlantPhoto> recentPhotos = photoService.list(photoWrapper);
        response.setRecentPhotos(recentPhotos);

        // Fetch counts
        response.setTotalSchedules(careScheduleService.count(new LambdaQueryWrapper<CareSchedule>().eq(CareSchedule::getPlantId, plantId).eq(CareSchedule::getPlantSource, "LOCAL")));
        response.setTotalRecords(careRecordService.count(new LambdaQueryWrapper<CareRecord>().eq(CareRecord::getPlantId, plantId).eq(CareRecord::getPlantSource, "LOCAL")));
        response.setTotalPhotos(photoService.count(new LambdaQueryWrapper<PlantPhoto>().eq(PlantPhoto::getPlantId, plantId).eq(PlantPhoto::getPlantSource, "LOCAL")));

        return response;
    }
}
