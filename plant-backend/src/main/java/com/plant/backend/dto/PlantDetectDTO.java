package com.plant.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 植物识别 DTO
 */
public class PlantDetectDTO {

    /**
     * 识别请求
     */
    @Data
    public static class DetectRequest {
        /**
         * 图片 Base64 编码
         */
        private String image;

        /**
         * 图片 URL（与 image 二选一）
         */
        private String imageUrl;
    }

    /**
     * 识别响应
     */
    @Data
    public static class DetectResponse {
        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 识别结果列表
         */
        private List<PlantResult> results;

        /**
         * 植物识别结果
         */
        @Data
        public static class PlantResult {
            /**
             * 植物名称
             */
            private String name;

            /**
             * 置信度 (0-1)
             */
            private Double score;

            /**
             * 百科链接
             */
            private String baikeUrl;

            /**
             * 植物分类信息
             */
            private String classification;
        }
    }
}
