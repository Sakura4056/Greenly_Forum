package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.OfficialPlant;

public interface PlantService {
    /**
     * 查询官方植物库
     */
    Page<OfficialPlant> queryOfficial(PlantDTO.OfficialQuery query);
    
    /**
     * 获取官方植物详情
     */
    OfficialPlant getOfficialDetail(Long id);
}
