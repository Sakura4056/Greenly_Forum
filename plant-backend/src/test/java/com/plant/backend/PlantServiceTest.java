package com.plant.backend;

import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.mapper.OfficialPlantMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 植物服务测试
 */
@SpringBootTest
public class PlantServiceTest {

    @Autowired
    private OfficialPlantMapper officialPlantMapper;

    @Test
    public void testGetOfficialPlant() {
        // 测试获取 ID=1 的官方植物
        OfficialPlant plant = officialPlantMapper.selectById(1L);
        
        System.out.println("=== 测试获取官方植物 ===");
        System.out.println("ID: " + plant.getId());
        System.out.println("名称：" + plant.getName());
        System.out.println("属：" + plant.getGenus());
        System.out.println("种：" + plant.getSpecies());
        System.out.println("描述：" + plant.getDescription());
        System.out.println("图片 URL: " + plant.getImageUrl());
        System.out.println("光照需求：" + plant.getLightReq());
        System.out.println("浇水频率：" + plant.getWaterReq());
        
        assert plant != null : "植物不应为空";
        assert plant.getId().equals(1L) : "ID 应该为 1";
    }
}
