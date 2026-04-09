package com.plant.backend.service.impl;

import com.plant.backend.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 天气服务实现类
 * <p>
 * 集成第三方天气 API（以和风天气为例），提供天气查询和浇水建议
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Override
    public Map<String, Object> getCurrentWeather(String city) {
        log.info("获取城市天气: {}", city);

        // 模拟天气数据（实际应调用 API）
        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", 25.0);
        weather.put("humidity", 65.0);
        weather.put("precipitation", 0.0);
        weather.put("precipProbability", 10);
        weather.put("condition", "晴");
        weather.put("windSpeed", 3.5);

        log.info("天气数据: temp={}°C, humidity={}%, precipProb={}%",
                weather.get("temperature"), weather.get("humidity"), weather.get("precipProbability"));

        return weather;
    }

    @Override
    public List<Map<String, Object>> getWeatherForecast(String city, int days) {
        log.info("获取城市天气预报: {}, 天数: {}", city, days);

        List<Map<String, Object>> forecast = new ArrayList<>();
        Random random = new Random();

        // 模拟未来几天的天气预报
        for (int i = 0; i < Math.min(days, 7); i++) {
            Map<String, Object> dayForecast = new HashMap<>();
            dayForecast.put("date", java.time.LocalDate.now().plusDays(i).toString());
            dayForecast.put("tempMax", 25 + random.nextInt(10));
            dayForecast.put("tempMin", 15 + random.nextInt(8));
            dayForecast.put("humidity", 50 + random.nextInt(40));
            dayForecast.put("precipProbability", random.nextInt(100));
            dayForecast.put("condition", random.nextBoolean() ? "晴" : "雨");
            forecast.add(dayForecast);
        }

        return forecast;
    }

    @Override
    public Map<String, Object> shouldWaterToday(String city) {
        log.info("判断是否适合浇水: {}", city);

        Map<String, Object> weather = getCurrentWeather(city);
        Map<String, Object> result = new HashMap<>();

        double temperature = (double) weather.get("temperature");
        double humidity = (double) weather.get("humidity");
        int precipProbability = (int) weather.get("precipProbability");

        boolean shouldWater = true;
        List<String> reasons = new ArrayList<>();

        // 判断逻辑 1: 降雨概率 > 60%，不建议浇水
        if (precipProbability > 60) {
            shouldWater = false;
            reasons.add(String.format("今日降雨概率 %d%%，建议等待自然降水", precipProbability));
        }

        // 判断逻辑 2: 湿度 > 80%，不建议浇水
        if (humidity > 80) {
            shouldWater = false;
            reasons.add(String.format("空气湿度 %.0f%% 较高，土壤可能 already 湿润", humidity));
        }

        // 判断逻辑 3: 温度 < 10°C，减少浇水
        if (temperature < 10) {
            shouldWater = false;
            reasons.add(String.format("气温 %.1f°C 较低，植物生长缓慢，建议减少浇水", temperature));
        }

        // 判断逻辑 4: 温度 > 35°C，建议在早晚浇水
        if (temperature > 35) {
            reasons.add("气温较高，建议在清晨或傍晚浇水，避免中午高温时段");
        }

        result.put("shouldWater", shouldWater);
        result.put("reasons", reasons);
        result.put("weather", weather);

        log.info("浇水建议: shouldWater={}, reasons={}", shouldWater, reasons);

        return result;
    }
}
