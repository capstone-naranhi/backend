-- ============================================================
-- V20260425_1__init_schema.sql
-- ============================================================

CREATE TABLE member
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(20)  NOT NULL,
    nickname     VARCHAR(20)  NOT NULL,
    email        VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(10)  NOT NULL DEFAULT 'USER',
    img_url      VARCHAR(255) NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE child
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    name       VARCHAR(20) NOT NULL,
    birth_date DATE        NOT NULL,
    gender     VARCHAR(6)  NOT NULL COMMENT 'MALE|FEMALE',
    img_url    VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_child_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE fcm_token
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    member_id     BIGINT       NOT NULL,
    token         VARCHAR(500) NOT NULL,
    device_id     VARCHAR(100) NOT NULL COMMENT '디바이스 식별자(기기를 구분)',
    platform_type VARCHAR(10)  NOT NULL COMMENT 'ANDROID|IOS|WEB',
    active        BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '토큰 활성화 여부(인증 시 false)',
    last_used_at  DATETIME(6) NULL COMMENT '마지막 사용 시간',
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fcm_token_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE device
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    device_name          VARCHAR(20)  NOT NULL COMMENT '보드 이름',
    device_serial_number VARCHAR(100) NOT NULL COMMENT '보드 시리얼 번호',
    location_name        VARCHAR(100) NOT NULL COMMENT '장소 이름',
    board_status         VARCHAR(10)  NOT NULL DEFAULT 'OFFLINE' COMMENT 'ONLINE|OFFLINE',
    camera_status        VARCHAR(10)  NOT NULL DEFAULT 'OFFLINE' COMMENT 'ONLINE|OFFLINE',
    mic_status           VARCHAR(10)  NOT NULL DEFAULT 'OFFLINE' COMMENT 'ONLINE|OFFLINE',
    mqtt_client_id       VARCHAR(100) NOT NULL COMMENT 'mqtt client id',
    last_heartbeat_at    DATETIME(6) NULL COMMENT '마지막 하트비트 시간',
    last_event_at        DATETIME(6) NULL COMMENT '마지막 이벤트 시간',
    created_at           DATETIME(6)  NOT NULL,
    updated_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_device_serial (device_serial_number),
    UNIQUE KEY uq_device_mqtt_client (mqtt_client_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE member_device
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    device_id  BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_device_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_member_device_device FOREIGN KEY (device_id) REFERENCES device (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE notification_setting
(
    notification_setting_id          BIGINT      NOT NULL AUTO_INCREMENT,
    member_id                        BIGINT      NOT NULL,
    is_received_safety_notification  BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '안전 알림 수신 여부',
    is_received_device_notification  BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '장치 알림 수신 여부',
    is_received_report_notification  BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '리포트 알림 수신 여부',
    is_received_general_notification BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '일반 알림 수신 여부',
    is_interference_active           BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '방해금지 활성화 여부',
    start_time                       TIME NULL COMMENT '방해금지 시작 시간',
    interference_end_time            TIME NULL COMMENT '방해금지 종료 시간',
    created_at                       DATETIME(6) NOT NULL,
    updated_at                       DATETIME(6) NOT NULL,
    PRIMARY KEY (notification_setting_id),
    CONSTRAINT fk_notif_setting_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE notification
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    type       VARCHAR(10) NOT NULL COMMENT 'SAFETY|DEVICE|GENERAL',
    sent_at    DATETIME(6) NOT NULL COMMENT '발송 시간',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE notification_recipient
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    notification_id BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    is_read         BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    read_at         DATETIME(6) NULL COMMENT '읽은 시간',
    is_sent         BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '전송 여부',
    sent_at         DATETIME(6) NULL COMMENT '전송 시간',
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_recipient_notification FOREIGN KEY (notification_id) REFERENCES notification (id),
    CONSTRAINT fk_notif_recipient_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE general_notification
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    notification_id BIGINT       NOT NULL,
    detail_type     VARCHAR(20)  NOT NULL COMMENT 'REPORT_AD',
    title           VARCHAR(100) NOT NULL COMMENT '제목',
    content         VARCHAR(500) NULL COMMENT '내용',
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_general_notif_notification FOREIGN KEY (notification_id) REFERENCES notification (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE safety_event
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    device_id       BIGINT      NOT NULL,
    event_type      VARCHAR(30) NOT NULL COMMENT 'UPSIDE_DOWN_SUFFOCATION|CRYING|FALL|BLANKET_SUFFOCATION|SCREAM|CLIMBING|WHINING',
    severity        VARCHAR(10) NOT NULL COMMENT 'DANGER|CAUTION|INFO',
    confidence      DECIMAL(5, 4) NULL COMMENT '신뢰 스코어',
    duration_second INT NULL COMMENT '지속 시간(초)',
    detected_at     DATETIME(6) NOT NULL COMMENT '이벤트 발생 시간',
    snapshot_url    VARCHAR(255) NULL COMMENT '이미지 url',
    video_url       VARCHAR(255) NULL COMMENT '녹화 영상 url',
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_safety_event_device FOREIGN KEY (device_id) REFERENCES device (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE device_notification
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    notification_id BIGINT      NOT NULL,
    device_id       BIGINT      NOT NULL,
    component_type  VARCHAR(10) NOT NULL COMMENT 'BOARD|CAMERA|MIC',
    before_status   VARCHAR(10) NOT NULL COMMENT 'ONLINE|OFFLINE|ERROR',
    current_status  VARCHAR(10) NOT NULL COMMENT 'ONLINE|OFFLINE|ERROR',
    description     VARCHAR(20) NULL COMMENT '설명',
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_device_notif_notification FOREIGN KEY (notification_id) REFERENCES notification (id),
    CONSTRAINT fk_device_notif_device FOREIGN KEY (device_id) REFERENCES device (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE safety_notification
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    notification_id BIGINT      NOT NULL,
    safety_event_id BIGINT      NOT NULL,
    device_id       BIGINT      NOT NULL COMMENT '이벤트 발생한 보드 장치',
    event_type      VARCHAR(30) NOT NULL COMMENT 'UPSIDE_DOWN_SUFFOCATION|CRYING|FALL|BLANKET_SUFFOCATION|SCREAM|CLIMBING|WHINING',
    severity        VARCHAR(10) NOT NULL COMMENT 'DANGER|CAUTION|INFO',
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_safety_notif_notification FOREIGN KEY (notification_id) REFERENCES notification (id),
    CONSTRAINT fk_safety_notif_event FOREIGN KEY (safety_event_id) REFERENCES safety_event (id),
    CONSTRAINT fk_safety_notif_device FOREIGN KEY (device_id) REFERENCES device (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
