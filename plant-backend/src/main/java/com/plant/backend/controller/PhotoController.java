package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.dto.PhotoDTO;
import com.plant.backend.entity.PlantPhoto;
import com.plant.backend.service.PhotoService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@RestController
@RequestMapping("/api/photo")
@RequiredArgsConstructor
public class PhotoController extends BaseController {

    private final PhotoService photoService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upload")
    public Result<PlantPhoto> upload(PhotoDTO.UploadRequest request, @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        log.info("=== 照片上传请求 ===");
        log.info("request: {}", request);
        log.info("file: {}, size: {}", file.getOriginalFilename(), file.getSize());
        
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        log.info("userId from token: {}", userId);
        
        request.setUserId(userId);
        log.info("设置后的 request: userId={}, plantId={}, plantSource={}", 
            request.getUserId(), request.getPlantId(), request.getPlantSource());

        PlantPhoto photo = photoService.upload(request, file);
        if (photo == null) {
            // Null 意味着分片已处理但尚未完成
            // 返回成功并带有 null 数据以指示 "分片已接收"
            return Result.success();
        }
        return success(photo);
    }

    @PostMapping("/update")
    public Result<PlantPhoto> update(
            @RequestBody @Validated PhotoDTO.UpdateRequest request,
            HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        return success(photoService.updatePhoto(request, userId));
    }

    @GetMapping("/query")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<PlantPhoto>> query(PhotoDTO.Query query,
            HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        String role = jwtUtil.getRoleFromToken(token);

        return success(photoService.queryPhotos(query, userId, role));
    }

    @GetMapping("/view/{filename}")
    public void view(@PathVariable("filename") String filename, jakarta.servlet.http.HttpServletResponse response) {
        File file = photoService.getPhotoFile(filename);

        if (file == null || !file.exists()) {
            response.setStatus(404);
            return;
        }

        response.setContentType("image/jpeg"); // Naive content type
        try (FileInputStream fis = new FileInputStream(file);
                OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        photoService.deletePhoto(id, userId);
        return success();
    }
}
