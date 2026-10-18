-- =============================================================
-- 图书馆自习室预约系统 建库建表脚本（MySQL 8.0）
-- 说明：所有表均使用 IF NOT EXISTS，可重复执行；
--      初始业务数据由后端 DataInitializer 在首次启动时写入。
-- =============================================================
CREATE DATABASE IF NOT EXISTS library_booking DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE library_booking;

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  role_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name   VARCHAR(20)  NOT NULL COMMENT '角色名称',
  role_key    VARCHAR(20)  NOT NULL COMMENT '角色标识',
  description VARCHAR(100) DEFAULT NULL COMMENT '角色描述',
  create_time DATETIME     NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  user_id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username         VARCHAR(50)  NOT NULL COMMENT '学号/登录账号',
  password         VARCHAR(100) NOT NULL COMMENT '加密密码',
  name             VARCHAR(20)  NOT NULL COMMENT '真实姓名',
  nickname         VARCHAR(20)  DEFAULT NULL COMMENT '昵称',
  phone            VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  avatar           VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  role_id          BIGINT       NOT NULL COMMENT '角色ID',
  credit_score     INT          NOT NULL DEFAULT 100 COMMENT '信用分',
  is_disabled      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否禁用预约：0否 1是',
  disable_end_time DATETIME     DEFAULT NULL COMMENT '禁用到期时间',
  status           TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
  create_time      DATETIME     NOT NULL COMMENT '创建时间',
  update_time      DATETIME     NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_username (username),
  KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 楼层表
CREATE TABLE IF NOT EXISTS library_floor (
  floor_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  floor_num  INT          NOT NULL COMMENT '楼层号（1-5）',
  floor_name VARCHAR(50)  NOT NULL COMMENT '楼层名称',
  description VARCHAR(255) DEFAULT NULL COMMENT '楼层说明',
  create_time DATETIME    NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_floor_num (floor_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层表';

-- 自习室表
CREATE TABLE IF NOT EXISTS study_room (
  room_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  floor_id    BIGINT       NOT NULL COMMENT '所属楼层ID',
  room_name   VARCHAR(50)  NOT NULL COMMENT '自习室名称',
  seat_count  INT          NOT NULL DEFAULT 0 COMMENT '座位总数',
  open_time   TIME         NOT NULL COMMENT '开放时间',
  close_time  TIME         NOT NULL COMMENT '关闭时间',
  room_type   VARCHAR(20)  NOT NULL DEFAULT '普通' COMMENT '类型：普通/静音/研讨',
  description VARCHAR(255) DEFAULT NULL COMMENT '简介说明',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1开放',
  create_time DATETIME     NOT NULL COMMENT '创建时间',
  update_time DATETIME     NOT NULL COMMENT '更新时间',
  KEY idx_floor (floor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室表';

-- 座位表
CREATE TABLE IF NOT EXISTS study_seat (
  seat_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id     BIGINT      NOT NULL COMMENT '所属自习室ID',
  seat_code   VARCHAR(20) NOT NULL COMMENT '座位编号',
  seat_type   VARCHAR(20) NOT NULL DEFAULT '普通' COMMENT '座位类型：靠窗/带插座/普通',
  position_x  INT         NOT NULL COMMENT '平面图X坐标',
  position_y  INT         NOT NULL COMMENT '平面图Y坐标',
  has_socket  TINYINT     NOT NULL DEFAULT 0 COMMENT '是否有插座',
  status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0不可用 1可用',
  create_time DATETIME    NOT NULL COMMENT '创建时间',
  KEY idx_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- 预约订单表
CREATE TABLE IF NOT EXISTS booking_order (
  order_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT NOT NULL COMMENT '用户ID',
  seat_id       BIGINT NOT NULL COMMENT '座位ID',
  room_id       BIGINT NOT NULL COMMENT '自习室ID',
  booking_date  DATE   NOT NULL COMMENT '预约日期',
  start_time    TIME   NOT NULL COMMENT '开始时间',
  end_time      TIME   NOT NULL COMMENT '结束时间',
  status        TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待签到 1进行中 2已完成 3已取消 4已爽约',
  checkin_time  DATETIME DEFAULT NULL COMMENT '签到时间',
  checkout_time DATETIME DEFAULT NULL COMMENT '签退时间',
  remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time   DATETIME NOT NULL COMMENT '创建时间',
  update_time   DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_seat_date (seat_id, booking_date, status),
  KEY idx_user_date (user_id, booking_date),
  KEY idx_status_date (status, booking_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 违规记录表
CREATE TABLE IF NOT EXISTS violation_record (
  violation_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL COMMENT '用户ID',
  order_id       BIGINT       DEFAULT NULL COMMENT '关联订单ID',
  violation_type VARCHAR(20)  NOT NULL COMMENT '违规类型：爽约/占座/其他',
  violation_desc VARCHAR(255) DEFAULT NULL COMMENT '违规描述',
  deduct_score   INT          NOT NULL COMMENT '扣减信用分',
  create_by      BIGINT       DEFAULT NULL COMMENT '操作人（NULL为系统）',
  create_time    DATETIME     NOT NULL COMMENT '违规时间',
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- 禁用记录表
CREATE TABLE IF NOT EXISTS disable_record (
  disable_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL COMMENT '用户ID',
  disable_reason VARCHAR(255) DEFAULT NULL COMMENT '禁用原因',
  start_time     DATETIME     NOT NULL COMMENT '禁用开始时间',
  end_time       DATETIME     NOT NULL COMMENT '禁用结束时间',
  is_lifted      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已解除：0否 1是',
  lift_by        BIGINT       DEFAULT NULL COMMENT '解除操作人（NULL为系统）',
  lift_time      DATETIME     DEFAULT NULL COMMENT '解除时间',
  create_time    DATETIME     NOT NULL COMMENT '创建时间',
  KEY idx_user_lift (user_id, is_lifted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁用记录表';

-- 公告表
CREATE TABLE IF NOT EXISTS sys_notice (
  notice_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(100) NOT NULL COMMENT '公告标题',
  content     TEXT         NOT NULL COMMENT '公告内容',
  is_top      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0下架 1发布',
  create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
  create_time DATETIME     NOT NULL COMMENT '创建时间',
  update_time DATETIME     NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
  config_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key   VARCHAR(50)  NOT NULL COMMENT '配置键',
  config_value VARCHAR(255) NOT NULL COMMENT '配置值',
  description  VARCHAR(100) DEFAULT NULL COMMENT '配置说明',
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorite (
  favorite_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT   NOT NULL COMMENT '用户ID',
  room_id     BIGINT   NOT NULL COMMENT '自习室ID',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_user_room (user_id, room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 站内消息表
CREATE TABLE IF NOT EXISTS sys_message (
  msg_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL COMMENT '接收用户ID',
  title       VARCHAR(100) NOT NULL COMMENT '消息标题',
  content     VARCHAR(500) DEFAULT NULL COMMENT '消息内容',
  type        VARCHAR(20)  NOT NULL DEFAULT '系统' COMMENT '类型：预约/违规/系统',
  is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0否 1是',
  create_time DATETIME     NOT NULL COMMENT '创建时间',
  KEY idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
  log_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       DEFAULT NULL COMMENT '操作人ID',
  module      VARCHAR(50)  DEFAULT NULL COMMENT '操作模块',
  content     VARCHAR(500) DEFAULT NULL COMMENT '操作内容',
  ip          VARCHAR(50)  DEFAULT NULL COMMENT '操作IP',
  create_time DATETIME     NOT NULL COMMENT '操作时间',
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
