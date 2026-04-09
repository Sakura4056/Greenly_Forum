package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.OfficialPlant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 植物服务单元测试
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("植物服务测试")
class PlantServiceTest {

    @Autowired
    private PlantService plantService;

    @Test
    @DisplayName("测试查询官方植物库 - 正常流程")
    void testQueryOfficial_Success() {
        // Given
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        // When
        Page<OfficialPlant> result = plantService.queryOfficial(query);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @DisplayName("测试查询官方植物库 - 按名称搜索")
    void testQueryOfficial_ByKeyword() {
        // Given
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("绿萝");

        // When
        Page<OfficialPlant> result = plantService.queryOfficial(query);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试查询官方植物库 - 按分类筛选")
    void testQueryOfficial_ByCategory() {
        // Given
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setCategory("观叶植物");

        // When
        Page<OfficialPlant> result = plantService.queryOfficial(query);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试获取官方植物详情 - 正常流程")
    void testGetOfficialDetail_Success() {
        // Given - 先查询一个存在的植物ID
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(1);
        query.setPageSize(1);
        Page<OfficialPlant> page = plantService.queryOfficial(query);

        if (page.getTotal() > 0) {
            Long plantId = page.getRecords().get(0).getId();

            // When
            OfficialPlant result = plantService.getOfficialDetail(plantId);

            // Then
            assertNotNull(result);
            assertEquals(plantId, result.getId());
        }
    }

    @Test
    @DisplayName("测试获取官方植物详情 - 植物不存在")
    void testGetOfficialDetail_NotFound() {
        // When & Then
        assertNull(plantService.getOfficialDetail(99999L));
    }

    @Test
    @DisplayName("测试分页参数边界条件")
    void testQueryOfficial_PageBoundary() {
        // Given
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(0);
        query.setPageSize(100);

        // When
        Page<OfficialPlant> result = plantService.queryOfficial(query);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试空关键词搜索")
    void testQueryOfficial_EmptyKeyword() {
        // Given
        PlantDTO.OfficialQuery query = new PlantDTO.OfficialQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("");

        // When
        Page<OfficialPlant> result = plantService.queryOfficial(query);

        // Then
        assertNotNull(result);
    }
}
