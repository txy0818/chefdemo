-- ============================
-- 创建数据库
-- ============================
CREATE DATABASE IF NOT EXISTS chef_reservation_system
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE chef_reservation_system;

DROP TABLE IF EXISTS chef_audit_record;
DROP TABLE IF EXISTS user_status_record;
DROP TABLE IF EXISTS notification_record;
DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS wallet_record;
DROP TABLE IF EXISTS reservation_order;
DROP TABLE IF EXISTS chef_available_time;
DROP TABLE IF EXISTS wallet;
DROP TABLE IF EXISTS chef_profile;
DROP TABLE IF EXISTS user;

-- ============================
-- 1. 用户表
-- ============================
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
                      role TINYINT NOT NULL COMMENT '角色 1-管理员 2-厨师 3-普通用户',
                      avatar VARCHAR(255) COMMENT '头像URL',
                      phone VARCHAR(20) COMMENT '手机号',
                      status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-正常 2-冻结',
                      last_login_time BIGINT COMMENT '最后登录时间(毫秒)',
                      create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                      update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',
                      UNIQUE KEY uk_username (username)
) COMMENT='用户表';

-- ============================
-- 2. 厨师资料表
-- ============================
CREATE TABLE chef_profile (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                              user_id BIGINT NOT NULL COMMENT '厨师用户ID',
                              avatar VARCHAR(255) COMMENT '头像URL',
                              display_name VARCHAR(50) COMMENT '展示昵称',
                              real_name VARCHAR(50) COMMENT '真实姓名',
                              -- 三证图片使用JSON数组存储
                              id_card_imgs JSON COMMENT '身份证图片URL列表',
                              health_cert_imgs JSON COMMENT '健康证图片URL列表',
                              chef_cert_imgs JSON COMMENT '厨师证图片URL列表',

                              cuisine_type VARCHAR(50) COMMENT '擅长菜系(枚举ID集合 1,2,3)',
                              service_area VARCHAR(100) COMMENT '服务区域',
                              service_desc VARCHAR(500) COMMENT '服务说明/个人简介',
                              price INT COMMENT '每小时价格(分)',
                              min_people INT COMMENT '最少服务人数',
                              max_people INT COMMENT '最多服务人数',
                              age INT COMMENT '年龄',
                              gender TINYINT COMMENT '性别 1-男 2-女',
                              work_years INT COMMENT '从业年限',
                              phone VARCHAR(20) COMMENT '手机号',
                              score BIGINT COMMENT '评分(1-5分，默认满分5分, 100-500)',
                              audit_status TINYINT NOT NULL COMMENT '审核结果 1-待审核 2-通过 3-拒绝',
                              create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                              update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                              UNIQUE KEY uk_user_id (user_id),
                              KEY idx_cuisine_type (cuisine_type),
                              KEY idx_service_area (service_area),
                              KEY idx_price (price),
                              KEY idx_people_range (min_people, max_people)
) COMMENT='厨师资料表';

-- ============================
-- 3. 厨师可预约时间段表
-- ============================
CREATE TABLE chef_available_time (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '时间段ID',
                                     chef_id BIGINT NOT NULL COMMENT '厨师ID',
                                     start_time BIGINT NOT NULL COMMENT '可预约开始时间(毫秒)',
                                     end_time BIGINT NOT NULL COMMENT '可预约结束时间(毫秒)',
                                     status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-可预约 2-已过期 3-已删除',
                                     create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                                     update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                                     KEY idx_chef_time (chef_id, start_time, end_time)
) COMMENT='厨师可预约时间段表';

-- ============================
-- 4. 预约订单表
-- ============================
CREATE TABLE reservation_order (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
                                   user_id BIGINT NOT NULL COMMENT '下单用户ID',
                                   chef_id BIGINT NOT NULL COMMENT '厨师ID',
                                   chef_available_time_id BIGINT COMMENT '预约时间段ID',
                                   start_time BIGINT NOT NULL COMMENT '预约开始时间(毫秒)',
                                   end_time BIGINT NOT NULL COMMENT '预约结束时间(毫秒)',
                                   total_time BIGINT NOT NULL COMMENT '总时长(毫秒)',
                                   total_amount INT NOT NULL COMMENT '订单总金额(分)',
                                   people_count INT NOT NULL COMMENT '预约人数',
                                   special_requirements VARCHAR(500) COMMENT '特殊要求',
                                   contact_name VARCHAR(50) COMMENT '联系人',
                                   contact_phone VARCHAR(20) COMMENT '联系电话',
                                   contact_address VARCHAR(255) COMMENT '联系地址',
                                   status TINYINT NOT NULL COMMENT '订单状态 1-待支付 2-待接单 3-已接单 4-已拒单 5-已完成 6-已取消',
                                   pay_status TINYINT NOT NULL COMMENT '支付状态 1-未支付 2-已支付,3-支付失败，4-已退款',
                                   cancel_reason VARCHAR(255) COMMENT '取消/拒单原因',
                                   pay_deadline_time BIGINT COMMENT '支付截止时间(毫秒)',
                                   pay_time BIGINT COMMENT '支付时间(毫秒)',
                                   accept_time BIGINT COMMENT '接单时间(毫秒)',
                                   complete_time BIGINT COMMENT '完成时间(毫秒)',
                                   cancel_time BIGINT COMMENT '取消/拒单时间(毫秒)',
                                   create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                                   update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                                   KEY idx_user_id (user_id),
                                   KEY idx_chef_id (chef_id),
                                   KEY idx_chef_time_id (chef_available_time_id),
                                   KEY idx_time_range (chef_id, start_time, end_time)
) COMMENT='预约订单表';

-- ============================
-- 5. 钱包表
-- ============================
CREATE TABLE wallet (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '钱包ID',
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        balance BIGINT NOT NULL DEFAULT 0 COMMENT '当前余额(分)',
                        create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                        update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                        UNIQUE KEY uk_wallet_user (user_id)
) COMMENT='用户钱包表';

-- ============================
-- 6. 钱包流水表
-- ============================
CREATE TABLE wallet_record (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               reservation_order_id BIGINT NOT NULL DEFAULT 0 COMMENT '关联预约订单ID, 充值为0',
                               amount BIGINT NOT NULL COMMENT '变动金额(分)',
                               type TINYINT NOT NULL COMMENT '类型 1-充值 2-支付 3-退款',
                               create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                               update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                               KEY idx_user_id (user_id),
                               KEY idx_order_id (reservation_order_id)
) COMMENT='钱包流水记录表';

-- ============================
-- 7. 评论表
-- ============================
CREATE TABLE review (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
                        reservation_order_id BIGINT NOT NULL COMMENT '预约订单ID',
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        chef_id BIGINT NOT NULL COMMENT '厨师ID',
                        score BIGINT NOT NULL COMMENT '评分 1-5分, 100-500',
                        content VARCHAR(500) COMMENT '评价内容',
                        audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态 1-待审核 2-已通过 3-已驳回',
                        audit_reason VARCHAR(255) COMMENT '审核驳回原因',
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-正常 2-已删除',
                        create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                        update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                        UNIQUE KEY uk_review_order (reservation_order_id),
                        KEY idx_chef_id (chef_id),
                        KEY idx_order_id (reservation_order_id)
) COMMENT='评价表';

-- ============================
-- 8. 举报表
-- ============================
CREATE TABLE report (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '举报ID',
                        reservation_order_id BIGINT NOT NULL COMMENT '关联订单ID',
                        reporter_id BIGINT NOT NULL COMMENT '举报人ID',
                        target_user_id BIGINT NOT NULL COMMENT '被举报用户ID',
                        reason VARCHAR(255) NOT NULL COMMENT '举报原因',
                        process_result VARCHAR(255) COMMENT '处理结果',
                        processed_by BIGINT COMMENT '处理管理员ID',
                        status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态 1-待处理 2-举报属实 3-驳回',
                        create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                        update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                        KEY idx_order_id (reservation_order_id),
                        KEY idx_target_user (target_user_id)
) COMMENT='举报表';


-- ============================
-- 9. 厨师审核记录表
-- ============================
CREATE TABLE chef_audit_record (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核记录ID',
                                   chef_user_id BIGINT NOT NULL COMMENT '厨师用户ID',
                                   audit_status TINYINT NOT NULL COMMENT '审核结果 1-待审核 2-通过 3-拒绝',
                                   operator_id BIGINT COMMENT '操作管理员ID',
                                   reject_reason VARCHAR(255) COMMENT '审核拒绝原因',
                                   create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                                   audit_time BIGINT COMMENT '审核完成时间(毫秒)',

                                   KEY idx_chef_user_id (chef_user_id),
                                   KEY idx_operator_id (operator_id)
) COMMENT='厨师审核记录表';

-- ============================
-- 10. 用户状态操作记录表(日志记录，前端未体现)
-- ============================
CREATE TABLE user_status_record (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态记录ID',
                                    user_id BIGINT NOT NULL COMMENT '用户ID',
                                    before_status TINYINT NOT NULL COMMENT '变更前状态',
                                    after_status TINYINT NOT NULL COMMENT '变更后状态',
                                    reason VARCHAR(255) COMMENT '变更原因',
                                    operator_id BIGINT NOT NULL COMMENT '操作管理员ID',
                                    create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',

                                    KEY idx_user_id (user_id)
) COMMENT='用户状态操作记录表';

-- ============================
-- 11. 通知记录表
-- ============================
CREATE TABLE notification_record (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
                                     user_id BIGINT NOT NULL COMMENT '接收用户ID',
                                     title VARCHAR(100) NOT NULL COMMENT '通知标题',
                                     content VARCHAR(500) NOT NULL COMMENT '通知内容',
                                     read_status TINYINT NOT NULL DEFAULT 1 COMMENT '读取状态 1-未读 2-已读',
                                     create_time BIGINT NOT NULL COMMENT '创建时间(毫秒)',
                                     update_time BIGINT NOT NULL COMMENT '更新时间(毫秒)',

                                     KEY idx_user_read (user_id, read_status)
) COMMENT='通知记录表';


INSERT INTO user (username, password, role, avatar, phone, status, last_login_time, create_time, update_time) VALUES ('admin', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 1, '', '', 1, 1775643273031, 1775643273031, 1775643273031);

INSERT INTO user (username, password, role, avatar, phone, status, last_login_time, create_time, update_time) VALUES ('system', 'bbc5e661e106c6dcd8dc6dd186454c2fcba3c710fb4d8e71a60c93eaf077f073', 1, '', '', 2, 1775643273031, 1775643273031, 1775643273031);

