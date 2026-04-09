package com.plant.backend.service;

import com.plant.backend.entity.IdentifyHistory;
import java.util.List;

public interface IdentifyHistoryService {
    void saveHistory(Long userId, String imageUrl, String plantName, Double confidence, 
                     String baikeUrl, String classification, String rawResult);
    List<IdentifyHistory> getUserHistory(Long userId, int limit);
    void deleteHistory(Long userId, Long historyId);
    void clearHistory(Long userId);
}
