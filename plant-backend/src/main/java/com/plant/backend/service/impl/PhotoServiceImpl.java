package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.plant.backend.dto.PhotoDTO;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.entity.PlantPhoto;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.mapper.OfficialPlantMapper;
import com.plant.backend.mapper.PlantPhotoMapper;
import com.plant.backend.service.PhotoService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl extends ServiceImpl<PlantPhotoMapper, PlantPhoto> implements PhotoService {

    @Value("${app.file-upload-path}")
    private String uploadPath;

    private final OfficialPlantMapper officialPlantMapper;
    private final MyPlantMapper myPlantMapper;

    // 允许的图片文件扩展名
    private static final Pattern IMAGE_PATTERN = Pattern.compile(".*\\.(jpg|jpeg|png|gif|webp|bmp)$", Pattern.CASE_INSENSITIVE);
    
    // 文件名安全化：替换非法字符
    private static final Pattern UNSAFE_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    /**
     * 验证图片文件类型
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !IMAGE_PATTERN.matcher(originalFilename).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                "只支持图片文件格式（jpg, jpeg, png, gif, webp, bmp）");
        }
        
        // 检查文件大小（限制为 10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件大小不能超过 10MB");
        }
    }

    /**
     * 生成安全的文件名
     * 格式：UUID_清理后的原始文件名
     * 例如：abc123def_plant_photo.jpg
     */
    private String generateSafeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            // 如果没有提供文件名，使用时间戳
            return UUID.randomUUID().toString().replace("-", "") + ".jpg";
        }
        
        // 提取文件扩展名
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(lastDotIndex);
            originalFileName = originalFileName.substring(0, lastDotIndex);
        }
        
        // 清理文件名中的非法字符
        String safeName = UNSAFE_FILENAME_PATTERN.matcher(originalFileName).replaceAll("_");
        
        // 限制文件名长度（避免过长）
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }
        
        // 生成最终文件名：UUID_清理后的名称.扩展名
        return UUID.randomUUID().toString().replace("-", "") + "_" + safeName + extension;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantPhoto upload(PhotoDTO.UploadRequest request, MultipartFile file) {
        log.info("=== PhotoService.upload 开始 ===");
        log.info("request: {}", request);
        log.info("request.userId: {}", request != null ? request.getUserId() : "null");
        log.info("request.plantId: {}", request != null ? request.getPlantId() : "null");
        log.info("request.plantSource: {}", request != null ? request.getPlantSource() : "null");
        
        // 验证参数
        if (request == null) {
            log.error("请求参数为空");
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请求参数不能为空");
        }
        
        if (request.getUserId() == null) {
            log.error("userId 为空");
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        if (request.getPlantSource() == null || request.getPlantSource().trim().isEmpty()) {
            log.error("plantSource 为空");
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "植物来源不能为空");
        }
        
        // 验证文件类型和大小
        validateImageFile(file);

        boolean isChunked = request.getChunks() != null && request.getChunks() > 1;
        File finalFile = null;

        try {
            if (isChunked) {
                finalFile = handleChunk(file, request);
                if (finalFile == null) {
                    return null;
                }
            } else {
                // 使用安全的文件名生成策略
                String fileName = generateSafeFileName(request.getFileName());
                File dest = new File(uploadPath, fileName);
                if (!dest.isAbsolute()) {
                    dest = dest.getAbsoluteFile();
                }
                FileUtils.forceMkdirParent(dest);
                file.transferTo(dest);
                finalFile = dest;
            }

            LocalDateTime captureTime = extractCaptureTime(finalFile);

            PlantPhoto photo = new PlantPhoto();
            photo.setUserId(request.getUserId());
            // plantId 可以为 null（新增植物时先上传封面）
            if (request.getPlantId() != null && request.getPlantId() > 0) {
                photo.setPlantId(request.getPlantId());
            } else {
                log.info("plantId 为 null 或 0，将不设置该字段");
            }
            photo.setPlantSource(request.getPlantSource());
            photo.setIsPublic(request.getIsPublic() == null ? 0 : request.getIsPublic());
            photo.setRemarks(request.getRemarks());
            photo.setFilePath(finalFile.getAbsolutePath());
            photo.setUrl("/uploads/" + finalFile.getName());
            photo.setCaptureTime(captureTime != null ? captureTime : LocalDateTime.now());

            log.info("准备保存照片: userId={}, plantId={}, url={}", 
                photo.getUserId(), photo.getPlantId(), photo.getUrl());
            
            save(photo);
            
            log.info("照片保存成功: id={}", photo.getId());
            return photo;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "文件上传失败");
        } catch (Exception e) {
            log.error("照片上传过程中发生未知错误", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "照片上传失败: " + e.getMessage());
        }
    }

    private File handleChunk(MultipartFile file, PhotoDTO.UploadRequest request) throws IOException {
        // 验证参数
        if (request == null || request.getUserId() == null) {
            throw new IOException("用户ID不能为空");
        }
        
        // 使用安全的临时目录名
        String safeFileName = request.getFileName() != null ? request.getFileName() : "chunked_upload";
        String tempDirName = request.getUserId() + "_" + 
            UNSAFE_FILENAME_PATTERN.matcher(safeFileName).replaceAll("_");
        File tempDir = new File(uploadPath + "/temp/" + tempDirName);
        FileUtils.forceMkdir(tempDir);

        File chunkFile = new File(tempDir, String.valueOf(request.getChunk()));
        if (!chunkFile.isAbsolute()) {
            chunkFile = chunkFile.getAbsoluteFile();
        }
        file.transferTo(chunkFile);

        if (tempDir.listFiles() != null && tempDir.listFiles().length == request.getChunks()) {
            return mergeChunks(tempDir, request.getFileName());
        }

        return null;
    }

    private File mergeChunks(File tempDir, String originalFileName) throws IOException {
        // 使用安全的文件名生成策略
        String fileName = generateSafeFileName(originalFileName);
        File destFile = new File(uploadPath, fileName);

        try (var outputStream = new java.io.FileOutputStream(destFile, true)) {
            File[] chunks = tempDir.listFiles();
            if (chunks != null) {
                for (int i = 0; i < chunks.length; i++) {
                    File chunk = new File(tempDir, String.valueOf(i));
                    if (chunk.exists()) {
                        FileUtils.copyFile(chunk, outputStream);
                    }
                }
            }
        }

        FileUtils.deleteDirectory(tempDir);
        return destFile;
    }

    private LocalDateTime extractCaptureTime(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
            }
        } catch (Exception e) {
            log.warn("从{}中提取 EXIF 信息失败", file.getName());
        }
        return null;
    }

    @Override
    public Page<PlantPhoto> queryPhotos(PhotoDTO.Query query, Long currentUserId, String currentRole) {
        Page<PlantPhoto> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PlantPhoto> wrapper = new LambdaQueryWrapper<>();

        if (!"ADMIN".equals(currentRole)) {
            if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            if (query.getUserId() == null) {
                query.setUserId(currentUserId);
            }
        }

        if (query.getUserId() != null)
            wrapper.eq(PlantPhoto::getUserId, query.getUserId());
        if (query.getPlantId() != null)
            wrapper.eq(PlantPhoto::getPlantId, query.getPlantId());
        if (query.getPlantSource() != null)
            wrapper.eq(PlantPhoto::getPlantSource, query.getPlantSource());
        if (query.getIsPublic() != null)
            wrapper.eq(PlantPhoto::getIsPublic, query.getIsPublic());

        if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getKeyword()))
            wrapper.like(PlantPhoto::getRemarks, query.getKeyword());
        if (query.getStartDate() != null)
            wrapper.ge(PlantPhoto::getCaptureTime, query.getStartDate().atStartOfDay());
        if (query.getEndDate() != null)
            wrapper.le(PlantPhoto::getCaptureTime, query.getEndDate().atTime(23, 59, 59));

        wrapper.orderByDesc(PlantPhoto::getCaptureTime);

        Page<PlantPhoto> result = page(page, wrapper);

        // 填充植物名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (PlantPhoto photo : result.getRecords()) {
                if ("OFFICIAL".equals(photo.getPlantSource())) {
                    // 官方植物：从 OfficialPlant 表查询
                    com.plant.backend.entity.OfficialPlant p = officialPlantMapper.selectById(photo.getPlantId());
                    if (p != null) {
                        photo.setPlantName(p.getName());
                    }
                } else if ("LOCAL".equals(photo.getPlantSource())) {
                    // 本地/自定义植物：先查 MyPlant 获取 officialId
                    MyPlant myPlant = myPlantMapper.selectById(photo.getPlantId());
                    if (myPlant != null) {
                        // 如果有 officialId，查询官方植物名称
                        if (myPlant.getOfficialId() != null) {
                            com.plant.backend.entity.OfficialPlant officialPlant = officialPlantMapper.selectById(myPlant.getOfficialId());
                            if (officialPlant != null) {
                                photo.setPlantName(officialPlant.getName());
                            } else {
                                // 如果官方植物不存在，使用昵称
                                photo.setPlantName(myPlant.getNickname());
                            }
                        } else {
                            // 如果没有 officialId，使用昵称
                            photo.setPlantName(myPlant.getNickname());
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantPhoto updatePhoto(PhotoDTO.UpdateRequest request, Long userId) {
        PlantPhoto photo = getById(request.getId());
        if (photo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "照片不存在");
        }
        if (!photo.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (request.getRemarks() != null) {
            photo.setRemarks(request.getRemarks());
        }
        if (request.getIsPublic() != null) {
            photo.setIsPublic(request.getIsPublic());
        }

        updateById(photo);
        return photo;
    }

    @Override
    public File getPhotoFile(String filename) {
        if (filename.contains(".."))
            return null;
        return new File(uploadPath, filename);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long id, Long userId) {
        PlantPhoto photo = getById(id);
        if (photo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "照片不存在");
        }
        if (!photo.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (photo.getFilePath() != null) {
            File file = new File(photo.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        removeById(id);
    }
}
