package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plant.backend.dto.PlantDiaryDTO;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.entity.PlantDiary;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.mapper.PlantDiaryMapper;
import com.plant.backend.service.PlantDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Plant Diary Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlantDiaryServiceImpl implements PlantDiaryService {

    private final PlantDiaryMapper plantDiaryMapper;
    private final MyPlantMapper myPlantMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long createDiary(Long userId, PlantDiaryDTO.CreateRequest request) {
        log.info("Create diary for user {}, plant: {}", userId, request.getPlantId());

        // Verify plant ownership
        MyPlant plant = myPlantMapper.selectById(request.getPlantId());
        if (plant == null || !userId.equals(plant.getUserId())) {
            throw new BusinessException("植物不存在或无权限");
        }

        PlantDiary diary = new PlantDiary();
        diary.setUserId(userId);
        diary.setPlantId(request.getPlantId());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setMood(request.getMood() != null ? request.getMood() : "neutral");
        diary.setWeather(request.getWeather());
        diary.setDiaryDate(request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now());

        // Convert photo IDs to JSON
        if (request.getPhotoIds() != null && !request.getPhotoIds().isEmpty()) {
            try {
                diary.setPhotos(objectMapper.writeValueAsString(request.getPhotoIds()));
            } catch (Exception e) {
                log.error("Error serializing photo IDs: {}", e.getMessage());
            }
        }

        diary.setDeleted(0);
        diary.setCreateTime(LocalDateTime.now());
        diary.setUpdateTime(LocalDateTime.now());

        plantDiaryMapper.insert(diary);
        log.info("Diary created successfully, id: {}", diary.getId());

        return diary.getId();
    }

    @Override
    @Transactional
    public void updateDiary(Long userId, PlantDiaryDTO.UpdateRequest request) {
        log.info("Update diary {} for user {}", request.getId(), userId);

        PlantDiary diary = plantDiaryMapper.selectById(request.getId());
        if (diary == null || !userId.equals(diary.getUserId())) {
            throw new BusinessException("日记不存在或无权限");
        }

        if (request.getTitle() != null) {
            diary.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }
        if (request.getMood() != null) {
            diary.setMood(request.getMood());
        }
        if (request.getWeather() != null) {
            diary.setWeather(request.getWeather());
        }

        // Update photo IDs
        if (request.getPhotoIds() != null) {
            try {
                diary.setPhotos(objectMapper.writeValueAsString(request.getPhotoIds()));
            } catch (Exception e) {
                log.error("Error serializing photo IDs: {}", e.getMessage());
            }
        }

        diary.setUpdateTime(LocalDateTime.now());
        plantDiaryMapper.updateById(diary);

        log.info("Diary updated successfully");
    }

    @Override
    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        log.info("Delete diary {} for user {}", diaryId, userId);

        PlantDiary diary = plantDiaryMapper.selectById(diaryId);
        if (diary == null || !userId.equals(diary.getUserId())) {
            throw new BusinessException("日记不存在或无权限");
        }

        // 使用 MyBatis Plus 的逻辑删除功能
        boolean deleted = plantDiaryMapper.deleteById(diaryId) > 0;
        if (!deleted) {
            throw new BusinessException("删除失败");
        }

        log.info("Diary deleted successfully");
    }

    @Override
    public PlantDiaryDTO.DiaryResponse getDiaryById(Long userId, Long diaryId) {
        PlantDiary diary = plantDiaryMapper.selectById(diaryId);
        if (diary == null || !userId.equals(diary.getUserId()) || diary.getDeleted() == 1) {
            throw new BusinessException("日记不存在或无权限");
        }

        return convertToResponse(diary);
    }

    @Override
    public Page<PlantDiaryDTO.DiaryResponse> queryDiaries(Long userId, PlantDiaryDTO.QueryRequest request) {
        log.info("Query diaries for user {}, plantId: {}, mood: {}", userId, request.getPlantId(), request.getMood());

        Page<PlantDiary> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<PlantDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlantDiary::getUserId, userId)
               .eq(PlantDiary::getDeleted, 0);

        if (request.getPlantId() != null) {
            wrapper.eq(PlantDiary::getPlantId, request.getPlantId());
        }

        if (request.getMood() != null && !request.getMood().isEmpty()) {
            wrapper.eq(PlantDiary::getMood, request.getMood());
        }

        if (request.getStartDate() != null) {
            wrapper.ge(PlantDiary::getDiaryDate, LocalDate.parse(request.getStartDate()));
        }

        if (request.getEndDate() != null) {
            wrapper.le(PlantDiary::getDiaryDate, LocalDate.parse(request.getEndDate()));
        }

        wrapper.orderByDesc(PlantDiary::getDiaryDate)
               .orderByDesc(PlantDiary::getCreateTime);

        Page<PlantDiary> diaryPage = plantDiaryMapper.selectPage(page, wrapper);

        // Convert to response DTOs
        Page<PlantDiaryDTO.DiaryResponse> responsePage = new Page<>(diaryPage.getCurrent(), diaryPage.getSize(), diaryPage.getTotal());
        List<PlantDiaryDTO.DiaryResponse> responses = diaryPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responses);

        return responsePage;
    }

    @Override
    public List<PlantDiaryDTO.DiaryResponse> getDiariesByPlant(Long userId, Long plantId) {
        LambdaQueryWrapper<PlantDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlantDiary::getUserId, userId)
               .eq(PlantDiary::getPlantId, plantId)
               .eq(PlantDiary::getDeleted, 0)
               .orderByDesc(PlantDiary::getDiaryDate);

        List<PlantDiary> diaries = plantDiaryMapper.selectList(wrapper);
        return diaries.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getMoodStatistics(Long userId, Long plantId, String startDate, String endDate) {
        LambdaQueryWrapper<PlantDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlantDiary::getUserId, userId)
               .eq(PlantDiary::getDeleted, 0)
               .select(PlantDiary::getMood);

        if (plantId != null) {
            wrapper.eq(PlantDiary::getPlantId, plantId);
        }

        if (startDate != null) {
            wrapper.ge(PlantDiary::getDiaryDate, LocalDate.parse(startDate));
        }

        if (endDate != null) {
            wrapper.le(PlantDiary::getDiaryDate, LocalDate.parse(endDate));
        }

        List<PlantDiary> diaries = plantDiaryMapper.selectList(wrapper);

        // Count by mood
        Map<String, Integer> moodCount = new HashMap<>();
        for (PlantDiary diary : diaries) {
            String mood = diary.getMood() != null ? diary.getMood() : "neutral";
            moodCount.put(mood, moodCount.getOrDefault(mood, 0) + 1);
        }

        return moodCount;
    }

    /**
     * Convert entity to response DTO
     */
    private PlantDiaryDTO.DiaryResponse convertToResponse(PlantDiary diary) {
        PlantDiaryDTO.DiaryResponse response = new PlantDiaryDTO.DiaryResponse();
        response.setId(diary.getId());
        response.setUserId(diary.getUserId());
        response.setPlantId(diary.getPlantId());
        response.setTitle(diary.getTitle());
        response.setContent(diary.getContent());
        response.setMood(diary.getMood());
        response.setWeather(diary.getWeather());
        response.setDiaryDate(diary.getDiaryDate().toString());
        response.setCreateTime(diary.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Get plant nickname
        MyPlant plant = myPlantMapper.selectById(diary.getPlantId());
        if (plant != null) {
            response.setPlantNickname(plant.getNickname());
        }

        // Parse photo IDs
        if (diary.getPhotos() != null && !diary.getPhotos().isEmpty()) {
            try {
                List<Long> photoIds = objectMapper.readValue(diary.getPhotos(), new TypeReference<List<Long>>() {});
                response.setPhotoIds(photoIds);
            } catch (Exception e) {
                log.error("Error deserializing photo IDs: {}", e.getMessage());
                response.setPhotoIds(new ArrayList<>());
            }
        } else {
            response.setPhotoIds(new ArrayList<>());
        }

        return response;
    }
}
