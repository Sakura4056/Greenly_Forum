package com.plant.backend.service;

import java.util.Map;

/**
 * 天气服务接口
 * <p>
 * 提供天气查询功能，用于智能浇水提醒
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
public interface WeatherService {

    /**
     * 获取当前天气信息
     *
     * @param city 城市名称
     * @return 天气信息（温度、湿度、降雨概率等）
     */
    Map<String, Object> getCurrentWeather(String city);

    /**
     * 获取未来几天天气预报
     *
     * @param city 城市名称
     * @param days 天数（1-7）
     * @return 天气预报列表
     */
    java.util.List<Map<String, Object>> getWeatherForecast(String city, int days);

    /**
     * 判断是否适合浇水
     * <p>
     * 根据天气条件（降雨概率、温度、湿度）判断今天是否适合浇水
     * </p>
     *
     * @param city 城市名称
     * @return 是否适合浇水及原因
     */
    Map<String, Object> shouldWaterToday(String city);
}
