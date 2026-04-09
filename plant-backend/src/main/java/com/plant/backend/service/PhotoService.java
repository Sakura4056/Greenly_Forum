package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.PhotoDTO;
import com.plant.backend.entity.PlantPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface PhotoService extends IService<PlantPhoto> {
    PlantPhoto upload(PhotoDTO.UploadRequest request, MultipartFile file);

    Page<PlantPhoto> queryPhotos(PhotoDTO.Query query, Long currentUserId, String currentRole);

    PlantPhoto updatePhoto(PhotoDTO.UpdateRequest request, Long userId);

    File getPhotoFile(String filename);

    void deletePhoto(Long id, Long userId);
}
