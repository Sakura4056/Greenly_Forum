package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.mapper.OfficialPlantMapper;
import com.plant.backend.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {

    private final OfficialPlantMapper officialPlantMapper;

    @Override
    public Page<OfficialPlant> queryOfficial(PlantDTO.OfficialQuery query) {
        Page<OfficialPlant> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OfficialPlant> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(OfficialPlant::getName, query.getKeyword())
                    .or().like(OfficialPlant::getGenus, query.getKeyword())
                    .or().like(OfficialPlant::getSpecies, query.getKeyword());
        }

        return officialPlantMapper.selectPage(page, wrapper);
    }

    @Override
    public OfficialPlant getOfficialDetail(Long id) {
        return officialPlantMapper.selectById(id);
    }
}
