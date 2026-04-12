package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.CareScheduleDTO;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.CareScheduleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 养护计划服务单元测试
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("养护计划服务测试")
class CareScheduleServiceTest {

    @Autowired
    private CareScheduleService careScheduleService;

    @Autowired
    private CareScheduleMapper careScheduleMapper;

    private Long testUserId;
    private Long testPlantId;

    @BeforeEach
    void setUp() {
        testUserId = 999L;
        testPlantId = 1L;
    }

    @Test
    @DisplayName("测试添加养护计划 - 正常流程")
    void testAdd_Success() {
        // Given
        CareScheduleDTO.AddRequest request = new CareScheduleDTO.AddRequest();
        request.setUserId(testUserId);
        request.setPlantId(testPlantId);
        request.setPlantSource("official");
        request.setTaskName("浇水");
        request.setDueTime(LocalDateTime.now().plusDays(1));
        request.setRecurrenceType("daily");
        request.setRecurrenceInterval(1);

        // When
        CareSchedule result = careScheduleService.add(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUserId, result.getUserId());
        assertEquals("浇水", result.getTaskName());
        assertEquals(0, result.getStatus());
    }

    @Test
    @DisplayName("测试添加养护计划 - 任务名称自动拼接植物名")
    void testAdd_TaskNameAutoConcat() {
        // Given
        CareScheduleDTO.AddRequest request = new CareScheduleDTO.AddRequest();
        request.setUserId(testUserId);
        request.setPlantId(testPlantId);
        request.setPlantSource("official");
        request.setPlantName("绿萝");
        request.setTaskName("浇水");
        request.setDueTime(LocalDateTime.now().plusDays(1));
        request.setRecurrenceType("weekly");

        // When
        CareSchedule result = careScheduleService.add(request);

        // Then
        assertTrue(result.getTaskName().startsWith("绿萝"));
    }

    @Test
    @DisplayName("测试更新养护计划 - 正常流程")
    void testUpdateSchedule_Success() {
        // Given
        CareScheduleDTO.AddRequest addRequest = new CareScheduleDTO.AddRequest();
        addRequest.setUserId(testUserId);
        addRequest.setPlantId(testPlantId);
        addRequest.setPlantSource("official");
        addRequest.setTaskName("施肥");
        addRequest.setDueTime(LocalDateTime.now().plusDays(2));
        addRequest.setRecurrenceType("monthly");
        CareSchedule schedule = careScheduleService.add(addRequest);

        CareScheduleDTO.UpdateRequest updateRequest = new CareScheduleDTO.UpdateRequest();
        updateRequest.setId(schedule.getId());
        updateRequest.setTaskName("更新后的施肥任务");

        // When
        CareSchedule updated = careScheduleService.updateSchedule(updateRequest, testUserId);

        // Then
        assertEquals("更新后的施肥任务", updated.getTaskName());
    }

    @Test
    @DisplayName("测试更新养护计划 - 计划不存在")
    void testUpdateSchedule_NotFound() {
        // Given
        CareScheduleDTO.UpdateRequest request = new CareScheduleDTO.UpdateRequest();
        request.setId(99999L);
        request.setTaskName("测试");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            careScheduleService.updateSchedule(request, testUserId);
        });
    }

    @Test
    @DisplayName("测试更新养护计划 - 权限不足")
    void testUpdateSchedule_Forbidden() {
        // Given
        CareScheduleDTO.AddRequest addRequest = new CareScheduleDTO.AddRequest();
        addRequest.setUserId(testUserId);
        addRequest.setPlantId(testPlantId);
        addRequest.setPlantSource("official");
        addRequest.setTaskName("修剪");
        addRequest.setDueTime(LocalDateTime.now().plusDays(1));
        addRequest.setRecurrenceType("none");
        CareSchedule schedule = careScheduleService.add(addRequest);

        CareScheduleDTO.UpdateRequest request = new CareScheduleDTO.UpdateRequest();
        request.setId(schedule.getId());
        request.setTaskName("被他人修改");

        // When & Then - 其他用户尝试修改
        assertThrows(BusinessException.class, () -> {
            careScheduleService.updateSchedule(request, 888L);
        });
    }

    @Test
    @DisplayName("测试删除养护计划 - 正常流程")
    void testDeleteSchedule_Success() {
        // Given
        CareScheduleDTO.AddRequest addRequest = new CareScheduleDTO.AddRequest();
        addRequest.setUserId(testUserId);
        addRequest.setPlantId(testPlantId);
        addRequest.setPlantSource("official");
        addRequest.setTaskName("除草");
        addRequest.setDueTime(LocalDateTime.now().plusDays(1));
        addRequest.setRecurrenceType("none");
        CareSchedule schedule = careScheduleService.add(addRequest);

        // When
        careScheduleService.deleteSchedule(schedule.getId(), testUserId);

        // Then
        assertNull(careScheduleMapper.selectById(schedule.getId()));
    }

    @Test
    @DisplayName("测试查询养护计划 - 按用户ID")
    void testQuery_ByUserId() {
        // Given
        for (int i = 0; i < 3; i++) {
            CareScheduleDTO.AddRequest request = new CareScheduleDTO.AddRequest();
            request.setUserId(testUserId);
            request.setPlantId(testPlantId);
            request.setPlantSource("official");
            request.setTaskName("任务" + i);
            request.setDueTime(LocalDateTime.now().plusDays(i));
            request.setRecurrenceType("none");
            careScheduleService.add(request);
        }

        CareScheduleDTO.Query query = new CareScheduleDTO.Query();
        query.setUserId(testUserId);
        query.setPageNum(1);
        query.setPageSize(10);

        // When
        Page<CareSchedule> result = careScheduleService.query(query, testUserId, "USER");

        // Then
        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);
    }

    @Test
    @DisplayName("测试查询养护计划 - 关键词搜索")
    void testQuery_ByKeyword() {
        // Given
        CareScheduleDTO.AddRequest request = new CareScheduleDTO.AddRequest();
        request.setUserId(testUserId);
        request.setPlantId(testPlantId);
        request.setPlantSource("official");
        request.setTaskName("特殊浇水任务");
        request.setDueTime(LocalDateTime.now().plusDays(1));
        request.setRecurrenceType("none");
        careScheduleService.add(request);

        CareScheduleDTO.Query query = new CareScheduleDTO.Query();
        query.setUserId(testUserId);
        query.setKeyword("特殊");
        query.setPageNum(1);
        query.setPageSize(10);

        // When
        Page<CareSchedule> result = careScheduleService.query(query, testUserId, "USER");

        // Then
        assertTrue(result.getTotal() >= 1);
    }


    @Test
    @DisplayName("测试逾期标记逻辑")
    void testMarkOverdue_AutoUpdate() {
        // Given - 创建一个已过期的计划
        CareScheduleDTO.AddRequest request = new CareScheduleDTO.AddRequest();
        request.setUserId(testUserId);
        request.setPlantId(testPlantId);
        request.setPlantSource("official");
        request.setTaskName("逾期任务");
        request.setDueTime(LocalDateTime.now().minusDays(1));
        request.setRecurrenceType("none");
        CareSchedule schedule = careScheduleService.add(request);
        assertEquals(0, schedule.getStatus());

        // When - 查询时会自动标记逾期
        CareScheduleDTO.Query query = new CareScheduleDTO.Query();
        query.setUserId(testUserId);
        query.setPageNum(1);
        query.setPageSize(10);
        careScheduleService.query(query, testUserId, "USER");

        // Then
        CareSchedule updated = careScheduleMapper.selectById(schedule.getId());
        assertEquals(2, updated.getStatus());
    }
}
