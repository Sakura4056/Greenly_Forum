package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.entity.IdentifyHistory;
import com.plant.backend.mapper.IdentifyHistoryMapper;
import com.plant.backend.service.IdentifyHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentifyHistoryServiceImpl implements IdentifyHistoryService {

    private final IdentifyHistoryMapper mapper;

    @Override
    public void saveHistory(Long userId, String imageUrl, String plantName, Double confidence,
                            String baikeUrl, String classification, String rawResult) {
        IdentifyHistory record = new IdentifyHistory();
        record.setUserId(userId);
        record.setImageUrl(imageUrl != null && imageUrl.length() > 500 ? imageUrl.substring(0, 500) : imageUrl);
        record.setPlantName(plantName);
        record.setConfidence(confidence);
        record.setBaikeUrl(baikeUrl);
        record.setClassification(classification);
        record.setRawResult(rawResult);
        record.setCreateTime(LocalDateTime.now());
        mapper.insert(record);
        log.info("Saved identify history for user {}: plant={}", userId, plantName);
    }

    @Override
    public List<IdentifyHistory> getUserHistory(Long userId, int limit) {
        LambdaQueryWrapper<IdentifyHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdentifyHistory::getUserId, userId)
               .orderByDesc(IdentifyHistory::getCreateTime)
               .last("LIMIT " + Math.min(limit, 100));
        return mapper.selectList(wrapper);
    }

    @Override
    public void deleteHistory(Long userId, Long historyId) {
        LambdaQueryWrapper<IdentifyHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdentifyHistory::getId, historyId)
               .eq(IdentifyHistory::getUserId, userId);
        mapper.delete(wrapper);
    }

    @Override
    public void clearHistory(Long userId) {
        LambdaQueryWrapper<IdentifyHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdentifyHistory::getUserId, userId);
        mapper.delete(wrapper);
    }
}
