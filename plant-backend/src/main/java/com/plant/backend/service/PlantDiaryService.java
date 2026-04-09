package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDiaryDTO;

import java.util.List;
import java.util.Map;

/**
 * Plant Diary Service Interface
 */
public interface PlantDiaryService {

    /**
     * Create a new diary entry
     */
    Long createDiary(Long userId, PlantDiaryDTO.CreateRequest request);

    /**
     * Update diary entry
     */
    void updateDiary(Long userId, PlantDiaryDTO.UpdateRequest request);

    /**
     * Delete diary entry
     */
    void deleteDiary(Long userId, Long diaryId);

    /**
     * Get diary by ID
     */
    PlantDiaryDTO.DiaryResponse getDiaryById(Long userId, Long diaryId);

    /**
     * Query diaries with pagination
     */
    Page<PlantDiaryDTO.DiaryResponse> queryDiaries(Long userId, PlantDiaryDTO.QueryRequest request);

    /**
     * Get diaries by plant ID
     */
    List<PlantDiaryDTO.DiaryResponse> getDiariesByPlant(Long userId, Long plantId);

    /**
     * Get mood statistics
     */
    Map<String, Integer> getMoodStatistics(Long userId, Long plantId, String startDate, String endDate);
}
