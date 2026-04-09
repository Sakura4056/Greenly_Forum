package com.plant.backend.controller;

import com.plant.backend.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/health")
    public Result<String> health() {
        return Result.success("Plant Backend is Running!");
    }
}
