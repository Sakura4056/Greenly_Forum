package com.plant.backend.dto;

import lombok.Data;

public class PlantDTO {

    @Data
    public static class OfficialQuery {
        private String keyword;
        private String category;
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
}
