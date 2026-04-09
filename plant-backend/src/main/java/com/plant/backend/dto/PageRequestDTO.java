package com.plant.backend.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页请求 DTO
 * 用于统一处理分页参数
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Data
@Schema(description = "分页请求")
public class PageRequestDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 转换为 MyBatis Plus Page 对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 获取偏移量（用于 LIMIT 查询）
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 验证分页参数
     */
    public void validate() {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
    }
}
