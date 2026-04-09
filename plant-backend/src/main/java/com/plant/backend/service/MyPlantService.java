package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.MyPlantDTO;
import com.plant.backend.entity.MyPlant;

public interface MyPlantService extends IService<MyPlant> {
    Long addMyPlant(Long userId, MyPlantDTO.AddRequest request);
    void updateMyPlant(Long userId, Long plantId, MyPlantDTO.UpdateRequest request);
    void deleteMyPlant(Long userId, Long plantId);
    Page<MyPlant> getMyPlants(Long userId, MyPlantDTO.QueryRequest query);
    MyPlant getMyPlantById(Long userId, Long plantId);
    MyPlantDTO.DetailResponse getMyPlantDetail(Long userId, Long plantId);
}
