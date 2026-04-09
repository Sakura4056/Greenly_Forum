-- =================================================================
-- Greenly 植物养护管理系统 - 数据库初始化脚本（优化版）
-- 描述: 完整的数据库结构创建和初始数据导入
-- 数据库版本: MySQL 8.0+
-- 字符集: utf8mb4
-- 版本: 4.0 (全面优化版)
-- 日期: 2026-04-06
-- 优化内容:
--   1. 删除冗余索引，补充缺失的组合索引
--   2. 优化字段类型和默认值
--   3. 规范化枚举值存储
--   4. JSON字段评估与保留决策
-- =================================================================
-- ⚠️ 警告: 此脚本将删除并重新创建 greenly_db 数据库
-- 所有现有数据将会丢失！生产环境执行前请务必备份！
-- =================================================================

-- =================================================================
-- 第一部分：数据库创建
-- =================================================================

DROP DATABASE IF EXISTS greenly_db;
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE greenly_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================
-- 第二部分：数据表定义（共 14 张表）
-- =================================================================

-- ----------------------------
-- 表 1: sys_user (系统用户表)
-- 用途: 存储系统所有用户信息，包括管理员和普通用户
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `user_id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '用户 ID（主键）',
    `username`        varchar(50)  NOT NULL COMMENT '用户名（登录账号）',
    `password`        varchar(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname`        varchar(50)  DEFAULT NULL COMMENT '昵称',
    `email`           varchar(100) DEFAULT NULL COMMENT '邮箱地址',
    `phone`           varchar(20)  DEFAULT NULL COMMENT '手机号码',
    `avatar`          varchar(255) DEFAULT NULL COMMENT '头像 URL',
    `role`            varchar(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户, ADMIN-管理员',
    `status`          tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    `gender`          tinyint      NOT NULL DEFAULT 0 COMMENT '性别：0-未知, 1-男, 2-女',
    `birthday`        date         DEFAULT NULL COMMENT '生日',
    `signature`       varchar(200) DEFAULT NULL COMMENT '个性签名',
    `last_login_time` datetime     DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   varchar(50)  DEFAULT NULL COMMENT '最后登录 IP',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_status` (`role`, `status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ----------------------------
-- 表 2: official_plant (官方植物库)
-- 用途: 存储官方维护的植物百科数据，作为用户添加植物的参考模板
-- ----------------------------
DROP TABLE IF EXISTS `official_plant`;
CREATE TABLE `official_plant` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '植物 ID（主键）',
    `name`            varchar(100) NOT NULL COMMENT '植物名称',
    `genus`           varchar(100) DEFAULT NULL COMMENT '属名',
    `species`         varchar(100) DEFAULT NULL COMMENT '种名',
    `description`     text COMMENT '植物描述',
    `image_url`       varchar(500) DEFAULT NULL COMMENT '植物图片 URL',
    `light_req`       varchar(50)  DEFAULT NULL COMMENT '光照需求：全日照/半阴/散射光等',
    `water_req`       varchar(50)  DEFAULT NULL COMMENT '浇水频率：多/中/少',
    `temp_range`      varchar(50)  DEFAULT NULL COMMENT '适宜温度范围',
    `soil_req`        varchar(200) DEFAULT NULL COMMENT '土壤要求',
    `difficulty`      varchar(20)  DEFAULT NULL COMMENT '养护难度：简单/中等/困难',
    `bloom_season`    varchar(100) DEFAULT NULL COMMENT '花期',
    `common_diseases` varchar(500) DEFAULT NULL COMMENT '常见病虫害',
    `care_tips`       text COMMENT '养护技巧',
    `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='官方植物库';

-- ----------------------------
-- 表 3: my_plant (用户植物表)
-- 用途: 存储用户个人添加的植物信息，关联官方植物库
-- ----------------------------
DROP TABLE IF EXISTS `my_plant`;
CREATE TABLE `my_plant` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '植物 ID（主键）',
    `user_id`       bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `official_id`   bigint       DEFAULT NULL COMMENT '官方植物 ID（关联 official_plant.id，可为空）',
    `nickname`      varchar(100) NOT NULL COMMENT '植物昵称',
    `location`      varchar(50)  DEFAULT NULL COMMENT '摆放位置：阳台/客厅/卧室等',
    `source`        varchar(50)  DEFAULT NULL COMMENT '来源：购买/赠送/扦插等',
    `acquired_date` date         DEFAULT NULL COMMENT '获取日期',
    `status`        varchar(20)  DEFAULT 'HEALTHY' COMMENT '健康状态：HEALTHY-健康, SICK-生病, DYING-枯萎',
    `cover_url`     varchar(255) DEFAULT NULL COMMENT '封面图片 URL',
    `notes`         text COMMENT '备注说明',
    `deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_deleted` (`user_id`, `deleted`),
    KEY `idx_official_id` (`official_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户植物表';

-- ----------------------------
-- 表 4: care_schedule (养护计划表)
-- 用途: 存储用户的植物养护提醒计划，支持周期性任务
-- ----------------------------
DROP TABLE IF EXISTS `care_schedule`;
CREATE TABLE `care_schedule` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '计划 ID（主键）',
    `user_id`             bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `plant_id`            bigint       NOT NULL COMMENT '植物 ID（关联 my_plant.id）',
    `plant_source`        varchar(20)  NOT NULL COMMENT '植物来源：LOCAL-本地植物',
    `task_name`           varchar(100) NOT NULL COMMENT '任务名称：浇水/施肥/修剪等',
    `due_time`            datetime     NOT NULL COMMENT '到期时间',
    `status`              tinyint      DEFAULT 0 COMMENT '状态：0-待完成, 1-已完成, 2-已跳过',
    `recurrence_type`     varchar(20)  NOT NULL DEFAULT 'NONE' COMMENT '重复类型：NONE-不重复, DAY-每天, WEEK-每周, MONTH-每月',
    `recurrence_interval` int          NOT NULL DEFAULT 0 COMMENT '重复间隔（天/周/月）',
    `reminder_config`     text COMMENT '提醒配置（JSON 格式）',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_status_due` (`user_id`, `status`, `due_time`),
    KEY `idx_plant_source` (`plant_id`, `plant_source`),
    KEY `idx_recurrence` (`recurrence_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养护计划表';

-- ----------------------------
-- 表 5: care_record (养护记录表)
-- 用途: 记录用户每次对植物的养护操作历史
-- ----------------------------
DROP TABLE IF EXISTS `care_record`;
CREATE TABLE `care_record` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '记录 ID（主键）',
    `user_id`      bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `plant_id`     bigint       NOT NULL COMMENT '植物 ID（关联 my_plant.id）',
    `plant_source` varchar(20)  NOT NULL COMMENT '植物来源：LOCAL-本地植物',
    `schedule_id`  bigint       DEFAULT NULL COMMENT '关联的养护计划 ID（可为空）',
    `record_time`  datetime     NOT NULL COMMENT '养护时间',
    `operations`   text COMMENT '养护操作（JSON 格式）：{"water": true, "fertilize": false}',
    `remarks`      varchar(255) DEFAULT NULL COMMENT '备注说明',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_plant_time` (`user_id`, `plant_id`, `record_time`),
    KEY `idx_schedule` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养护记录表';

-- ----------------------------
-- 表 6: plant_photo (植物照片表)
-- 用途: 存储用户上传的植物照片，支持相册管理
-- ----------------------------
DROP TABLE IF EXISTS `plant_photo`;
CREATE TABLE `plant_photo` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '照片 ID（主键）',
    `user_id`      bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `plant_id`     bigint       DEFAULT NULL COMMENT '关联的植物 ID（可为空，上传封面时可能还未创建植物）',
    `plant_source` varchar(20)  NOT NULL COMMENT '植物来源：LOCAL-本地植物',
    `is_public`    tinyint(1)   DEFAULT 0 COMMENT '是否公开：0-私有, 1-公开',
    `remarks`      varchar(255) DEFAULT NULL COMMENT '照片说明',
    `url`          varchar(255) NOT NULL COMMENT '照片访问 URL',
    `file_path`    varchar(255) DEFAULT NULL COMMENT '文件存储路径',
    `capture_time` datetime     DEFAULT NULL COMMENT '拍摄时间',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_plant` (`user_id`, `plant_id`),
    KEY `idx_user_public` (`user_id`, `is_public`),
    KEY `idx_capture_time` (`capture_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='植物照片表';

-- ----------------------------
-- 表 7: reminder_config (提醒配置表)
-- 用途: 存储用户的提醒偏好设置，包括通知方式和场景配置
-- 注意: 邮箱地址统一从 sys_user 表获取，此处不重复存储
-- ----------------------------
DROP TABLE IF EXISTS `reminder_config`;
CREATE TABLE `reminder_config` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '配置 ID（主键）',
    `user_id`       bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `phone`         varchar(20)  DEFAULT NULL COMMENT '手机号码（用于未来短信通知扩展）',
    `popup_enabled` tinyint(1)   DEFAULT 1 COMMENT '弹窗提醒：0-关闭, 1-开启',
    `bell_enabled`  tinyint(1)   DEFAULT 1 COMMENT '声音提醒：0-关闭, 1-开启',
    `scene_config`  text COMMENT '场景配置（JSON 格式）：{"emailEnabled":true,"summaryTime":"09:00","doNotDisturb":false}',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒配置表';

-- ----------------------------
-- 表 8: reminder (消息提醒表)
-- 用途: 存储系统发送给用户的各类消息提醒
-- ----------------------------
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '提醒 ID（主键）',
    `user_id`     bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `scene`       varchar(50)  NOT NULL COMMENT '场景类型：careSchedule-养护提醒, system-系统通知',
    `business_id` bigint       DEFAULT NULL COMMENT '业务 ID（关联具体业务记录）',
    `title`       varchar(100) NOT NULL COMMENT '提醒标题',
    `content`     text COMMENT '提醒内容',
    `is_read`     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_scene_read` (`user_id`, `scene`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息提醒表';

-- ----------------------------
-- 表 9: plant_diary (植物日记表)
-- 用途: 记录用户养护植物的心得和成长历程
-- ----------------------------
DROP TABLE IF EXISTS `plant_diary`;
CREATE TABLE `plant_diary` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '日记 ID（主键）',
    `user_id`     bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `plant_id`    bigint       NOT NULL COMMENT '植物 ID（关联 my_plant.id）',
    `title`       varchar(200) DEFAULT NULL COMMENT '日记标题',
    `content`     text         NOT NULL COMMENT '日记内容',
    `mood`        varchar(20)  DEFAULT 'neutral' COMMENT '心情：happy-开心, sad-难过, neutral-平静, excited-兴奋, worried-担心',
    `weather`     varchar(20)  DEFAULT NULL COMMENT '天气：sunny-晴, cloudy-多云, rainy-雨, snowy-雪, windy-风',
    `photos`      json         DEFAULT NULL COMMENT '关联的照片 ID 列表（JSON 数组）',
    `diary_date`  date         NOT NULL COMMENT '日记日期',
    `deleted`     tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_plant_date` (`user_id`, `plant_id`, `diary_date`),
    KEY `idx_mood` (`mood`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='植物日记表';

-- ----------------------------
-- 表 10: ai_conversation (AI 对话历史表)
-- 用途: 存储用户与 AI 助手的对话记录
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '对话 ID（主键）',
    `user_id`      bigint       NOT NULL COMMENT '用户 ID（关联 sys_user.user_id）',
    `session_id`   varchar(100) NOT NULL COMMENT '会话 ID',
    `role`         varchar(20)  NOT NULL COMMENT '角色：user-用户, assistant-助手, system-系统',
    `content`      text         NOT NULL COMMENT '对话内容',
    `model`        varchar(50)  DEFAULT NULL COMMENT '使用的 AI 模型',
    `tokens_used`  int          DEFAULT NULL COMMENT '消耗的 Token 数量',
    `metadata`     json         DEFAULT NULL COMMENT '元数据（JSON 格式）',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_session` (`user_id`, `session_id`),
    KEY `idx_user_create` (`user_id`, `create_time`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话历史表';

-- ----------------------------
-- 表 11: announcement (系统公告表)
-- 用途: 存储管理员发布的系统公告信息
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '公告 ID（主键）',
    `title`         varchar(200) NOT NULL COMMENT '公告标题',
    `content`       text         NOT NULL COMMENT '公告内容',
    `publisher_id`  bigint       NOT NULL COMMENT '发布人 ID（关联 sys_user.user_id）',
    `publish_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：0-草稿, 1-已发布',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_status` (`publisher_id`, `status`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_status_deleted` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ----------------------------
-- 表 12: forum_post (论坛帖子表)
-- 用途: 存储用户发布的论坛帖子
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post` (
    `post_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '帖子 ID（主键）',
    `user_id`       bigint       NOT NULL COMMENT '发帖用户 ID',
    `title`         varchar(200) NOT NULL COMMENT '帖子标题',
    `content`       text         NOT NULL COMMENT '帖子内容',
    `view_count`    int          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `reply_count`   int          NOT NULL DEFAULT 0 COMMENT '回复数',
    `like_count`    int          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `is_top`        tinyint      NOT NULL DEFAULT 0 COMMENT '是否置顶：1-是, 0-否',
    `is_essence`    tinyint      NOT NULL DEFAULT 0 COMMENT '是否精华：1-是, 0-否',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-隐藏',
    `deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_is_essence` (`is_essence`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表';

-- ----------------------------
-- 表 13: forum_comment (论坛评论表)
-- 用途: 存储用户对帖子的评论和回复
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
    `comment_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '评论 ID（主键）',
    `post_id`         bigint       NOT NULL COMMENT '帖子 ID',
    `user_id`         bigint       NOT NULL COMMENT '评论用户 ID',
    `parent_id`       bigint       DEFAULT NULL COMMENT '父评论 ID（回复时填写）',
    `reply_to_user_id` bigint      DEFAULT NULL COMMENT '被回复的用户 ID',
    `content`         text         NOT NULL COMMENT '评论内容',
    `like_count`      int          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`          tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-隐藏',
    `deleted`         tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`comment_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_reply_to_user_id` (`reply_to_user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论表';

-- ----------------------------
-- 表 14: forum_like (论坛点赞表)
-- 用途: 记录用户对帖子或评论的点赞行为
-- ----------------------------
DROP TABLE IF EXISTS `forum_like`;
CREATE TABLE `forum_like` (
    `like_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '点赞 ID（主键）',
    `user_id`       bigint       NOT NULL COMMENT '点赞用户 ID',
    `target_type`   varchar(20)  NOT NULL COMMENT '目标类型：POST-帖子, COMMENT-评论',
    `target_id`     bigint       NOT NULL COMMENT '目标 ID（帖子ID或评论ID）',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`like_id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛点赞表';

-- =================================================================
-- 第三部分：初始数据导入
-- =================================================================

-- ----------------------------
-- 数据 1: 系统用户（4 个默认用户）
-- 密码: admin123 / user123 (BCrypt 加密哈希)
-- ----------------------------
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `email`, `phone`, `role`, `status`, `gender`, `signature`) VALUES
(1, 'admin', '$2a$10$D0WC2cOO9qc3.aQOW4ek5OusEkpvTGCHQTBDE1.ghcje5NzhJEXlO', '系统管理员', 'admin@greenly.com', '13800138000', 'ADMIN', 1, 1, 'Greenly 系统管理员'),
(2, 'user1', '$2a$10$D0WC2cOO9qc3.aQOW4ek5OusEkpvTGCHQTBDE1.ghcje5NzhJEXlO', '新手花友', 'user1@greenly.com', '13800138001', 'USER', 1, 1, '刚入门的园艺爱好者'),
(3, 'user2', '$2a$10$D0WC2cOO9qc3.aQOW4ek5OusEkpvTGCHQTBDE1.ghcje5NzhJEXlO', '资深花友', 'user2@greenly.com', '13800138002', 'USER', 1, 2, '多年养植经验的园艺达人'),
(4, 'gardener', '$2a$10$D0WC2cOO9qc3.aQOW4ek5OusEkpvTGCHQTBDE1.ghcje5NzhJEXlO', '园艺爱好者', 'gardener@greenly.com', '13800138003', 'USER', 1, 1, '热爱园艺生活');

-- ----------------------------
-- 数据 2: 官方植物库（5 种常见植物）
-- ----------------------------
INSERT INTO `official_plant` (`id`, `name`, `genus`, `species`, `description`, `image_url`, `light_req`, `water_req`, `temp_range`, `soil_req`, `difficulty`, `bloom_season`, `common_diseases`, `care_tips`) VALUES
(1, '绿萝', 'Epipremnum', 'Epipremnum aureum', '常绿藤本植物，心形叶片，具有强大的空气净化能力，适合室内养殖。', 'https://example.com/images/pothos.jpg', '散射光', '适中', '15-28°C', '疏松肥沃、排水良好的土壤', '简单', '全年', '根腐病、叶斑病', '保持土壤表面干燥后浇水，避免阳光直射。'),
(2, '多肉植物', 'Sedum', 'Sedum', '叶片、茎或根部储存水分的植物，耐旱性强，形态多样。', 'https://example.com/images/succulent.jpg', '全日照', '少量', '10-30°C', '沙质、排水良好的土壤', '简单', '春夏季', '介壳虫、根腐病', '浇水前确保土壤完全干透。'),
(3, '吊兰', 'Chlorophytum', 'Chlorophytum comosum', '常见的室内观叶植物，能有效去除空气中的有害物质。', 'https://example.com/images/spider-plant.jpg', '明亮散射光', '适中', '18-24°C', '排水良好的营养土', '简单', '夏季', '叶尖枯焦、根腐病', '定期浇水但避免积水。'),
(4, '发财树', 'Pachira', 'Pachira aquatica', '寓意招财进宝的室内观叶植物，广受欢迎。', 'https://example.com/images/money-tree.jpg', '明亮散射光', '适中', '18-27°C', '排水良好、泥炭基土壤', '中等', '室内罕见开花', '叶斑病、根腐病', '表层土壤干燥时浇水。'),
(5, '芦荟', 'Aloe', 'Aloe vera', '多肉植物，具有药用价值，常用于护肤和治疗轻微烧伤。', 'https://example.com/images/aloe-vera.jpg', '全日照', '少量', '13-27°C', '沙质或砾石质、排水良好', '简单', '夏季', '软腐病、介壳虫', '深度浇水但频率要低。');

-- ----------------------------
-- 数据 3: 用户植物（user1 的 3 盆植物）
-- ----------------------------
INSERT INTO `my_plant` (`id`, `user_id`, `official_id`, `nickname`, `location`, `source`, `acquired_date`, `status`, `cover_url`, `notes`) VALUES
(1, 2, 1, '我的绿萝', '阳台', '购买', '2024-01-01', 'HEALTHY', 'https://example.com/photos/pothos.jpg', '生长良好，每周浇水一次'),
(2, 2, 2, '多肉宝宝', '书桌', '朋友赠送', '2024-02-15', 'HEALTHY', 'https://example.com/photos/succulent.jpg', '喜欢阳光，耐旱'),
(3, 2, 3, '小吊兰', '客厅', '购买', '2024-03-10', 'HEALTHY', 'https://example.com/photos/spider-plant.jpg', '很好的空气净化器');

-- ----------------------------
-- 数据 4: 养护计划（3 个计划）
-- ----------------------------
INSERT INTO `care_schedule` (`id`, `user_id`, `plant_id`, `plant_source`, `task_name`, `due_time`, `status`, `recurrence_type`, `recurrence_interval`, `reminder_config`) VALUES
(1, 2, 1, 'LOCAL', '给绿萝浇水', '2024-04-05 10:00:00', 0, 'WEEK', 1, '{"notifyBefore": 1, "channels": ["popup", "email"]}'),
(2, 2, 2, 'LOCAL', '多肉晒太阳', '2024-04-06 09:00:00', 0, 'DAY', 1, '{"notifyBefore": 0, "channels": ["popup"]}'),
(3, 2, 3, 'LOCAL', '给吊兰施肥', '2024-04-10 14:00:00', 0, 'MONTH', 1, '{"notifyBefore": 2, "channels": ["popup", "email"]}');

-- ----------------------------
-- 数据 5: 养护记录（3 条记录）
-- ----------------------------
INSERT INTO `care_record` (`id`, `user_id`, `plant_id`, `plant_source`, `schedule_id`, `record_time`, `operations`, `remarks`) VALUES
(1, 2, 1, 'LOCAL', 1, '2024-03-29 10:30:00', '{"water": true, "fertilize": false, "prune": false}', '浇水 500ml'),
(2, 2, 2, 'LOCAL', NULL, '2024-03-30 15:00:00', '{"water": false, "fertilize": false, "prune": true}', '修剪枯叶'),
(3, 2, 3, 'LOCAL', NULL, '2024-04-01 14:30:00', '{"water": true, "fertilize": true, "prune": false}', '浇水并施肥');

-- ----------------------------
-- 数据 6: 植物照片（3 张照片）
-- ----------------------------
INSERT INTO `plant_photo` (`id`, `user_id`, `plant_id`, `plant_source`, `is_public`, `remarks`, `url`, `file_path`, `capture_time`) VALUES
(1, 2, 1, 'LOCAL', 0, '刚买回家时', 'https://example.com/photos/pothos-1.jpg', '/path/to/photos/pothos-1.jpg', '2024-01-01 12:00:00'),
(2, 2, 1, 'LOCAL', 0, '一个月后', 'https://example.com/photos/pothos-2.jpg', '/path/to/photos/pothos-2.jpg', '2024-02-01 12:00:00'),
(3, 2, 2, 'LOCAL', 1, '多肉特写', 'https://example.com/photos/succulent-1.jpg', '/path/to/photos/succulent-1.jpg', '2024-02-15 14:00:00');

-- ----------------------------
-- 数据 7: 提醒配置（2 个用户配置）
-- 注意: 邮箱地址已从 sys_user 表统一管理，此处不再存储
-- ----------------------------
INSERT INTO `reminder_config` (`user_id`, `phone`, `popup_enabled`, `bell_enabled`, `scene_config`) VALUES
(1, '13800138000', 1, 1, '{"careSchedule": {"enabled": true, "notifyBefore": 1}, "plantAudit": {"enabled": true}}'),
(2, '13800138001', 1, 1, '{"careSchedule": {"enabled": true, "notifyBefore": 1}, "plantAudit": {"enabled": true}}');

-- ----------------------------
-- 数据 8: 消息提醒（3 条通知）
-- ----------------------------
INSERT INTO `reminder` (`id`, `user_id`, `scene`, `business_id`, `title`, `content`, `is_read`) VALUES
(1, 2, 'careSchedule', 1, '养护提醒：给绿萝浇水', '您的绿萝需要浇水了，请及时处理。', 0),
(2, 2, 'careSchedule', 2, '养护提醒：多肉晒太阳', '今天是个好天气，让您的多肉晒晒太阳吧。', 0),
(3, 2, 'system', NULL, '系统通知', '您的账号已成功注册，欢迎使用 Greenly！', 1);

-- ----------------------------
-- 数据 9: 植物日记（5 篇日记）
-- ----------------------------
INSERT INTO `plant_diary` (`id`, `user_id`, `plant_id`, `title`, `content`, `mood`, `weather`, `photos`, `diary_date`) VALUES
(1, 2, 1, '绿萝的第一周', '我的绿萝在阳台上适应得很好，叶子翠绿有光泽，看起来非常健康。', 'happy', 'sunny', '[1]', '2024-01-08'),
(2, 2, 2, '多肉长得真快', '发现多肉长出了新芽！书桌上的阳光似乎很适合它。', 'excited', 'sunny', '[3]', '2024-02-22'),
(3, 2, 3, '吊兰生宝宝了', '我的吊兰今天长出了两株小苗！计划不久后进行繁殖。', 'happy', 'cloudy', NULL, '2024-03-15'),
(4, 2, 1, '黄叶担忧', '绿萝下部的一片叶子变黄了，可能是浇水过多？', 'worried', 'rainy', NULL, '2024-03-20'),
(5, 2, 2, '完美的多肉颜色', '今天早上发现多肉叶片边缘呈现出美丽的粉红色！', 'happy', 'sunny', NULL, '2024-04-01');

-- =================================================================
-- 第四部分：脚本执行完成与验证
-- =================================================================

SET FOREIGN_KEY_CHECKS = 1;

-- 验证查询
SELECT '=== 数据库初始化完成 ===' AS message;

SELECT '已创建的表:' AS info;
SHOW TABLES;

SELECT '数据统计:' AS info;
SELECT 
    'sys_user' AS table_name, COUNT(*) AS record_count FROM sys_user
UNION ALL SELECT 'official_plant', COUNT(*) FROM official_plant
UNION ALL SELECT 'my_plant', COUNT(*) FROM my_plant
UNION ALL SELECT 'care_schedule', COUNT(*) FROM care_schedule
UNION ALL SELECT 'care_record', COUNT(*) FROM care_record
UNION ALL SELECT 'plant_photo', COUNT(*) FROM plant_photo
UNION ALL SELECT 'reminder_config', COUNT(*) FROM reminder_config
UNION ALL SELECT 'reminder', COUNT(*) FROM reminder
UNION ALL SELECT 'plant_diary', COUNT(*) FROM plant_diary
UNION ALL SELECT 'ai_conversation', COUNT(*) FROM ai_conversation
UNION ALL SELECT 'announcement', COUNT(*) FROM announcement
UNION ALL SELECT 'forum_post', COUNT(*) FROM forum_post
UNION ALL SELECT 'forum_comment', COUNT(*) FROM forum_comment
UNION ALL SELECT 'forum_like', COUNT(*) FROM forum_like;

SELECT '字符集信息:' AS info;
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'greenly_db';

SELECT '✅ 数据库初始化成功！' AS final_message;
