-- ============================================================
-- V20260425_2__seed_data.sql  (local seed)
-- ============================================================

-- ── member ──────────────────────────────────────────────────
INSERT INTO member (name, nickname, email, phone_number, password, role, img_url, created_at, updated_at)
VALUES ('홍길동', '길동이', 'user@naranhi.com', '010-1234-5678',
        '$2a$10$dummy.hashed.password.user000000000000000000000', 'USER', NULL,
        NOW(6), NOW(6)),
       ('관리자', '어드민', 'admin@naranhi.com', '010-9999-0000',
        '$2a$10$dummy.hashed.password.admin00000000000000000000', 'ADMIN', NULL,
        NOW(6), NOW(6));

-- ── child ────────────────────────────────────────────────────
INSERT INTO child (member_id, name, birth_date, gender, img_url, created_at, updated_at)
VALUES (1, '홍아이', '2023-03-15', 'FEMALE', NULL, NOW(6), NOW(6)),
       (1, '홍둘째', '2024-07-20', 'MALE', NULL, NOW(6), NOW(6));

-- ── device ───────────────────────────────────────────────────
INSERT INTO device (device_name, device_serial_number, location_name, board_status, camera_status, mic_status,
                    mqtt_client_id, last_heartbeat_at, last_event_at, created_at, updated_at)
VALUES ('나란히-001', 'SN-2024-0001', '아이방', 'ONLINE', 'ONLINE', 'ONLINE',
        'mqtt-client-001', NOW(6), NULL, NOW(6), NOW(6)),
       ('나란히-002', 'SN-2024-0002', '거실', 'OFFLINE', 'OFFLINE', 'OFFLINE',
        'mqtt-client-002', NULL, NULL, NOW(6), NOW(6));

-- ── member_device ─────────────────────────────────────────────
INSERT INTO member_device (member_id, device_id, created_at, updated_at)
VALUES (1, 1, NOW(6), NOW(6)),
       (1, 2, NOW(6), NOW(6));

-- ── fcm_token ─────────────────────────────────────────────────
INSERT INTO fcm_token (member_id, token, device_id, platform_type, active, last_used_at, created_at, updated_at)
VALUES (1, 'fcm-token-sample-android-user1-000000000000000000000000000000', 'device-uuid-android-001', 'ANDROID',
        TRUE, NOW(6), NOW(6), NOW(6)),
       (1, 'fcm-token-sample-ios-user1-0000000000000000000000000000000000', 'device-uuid-ios-001', 'IOS',
        FALSE, NOW(6), NOW(6), NOW(6));

-- ── notification_setting ──────────────────────────────────────
INSERT INTO notification_setting (member_id, is_received_safety_notification, is_received_device_notification,
                                  is_received_report_notification, is_received_general_notification,
                                  is_interference_active, start_time, interference_end_time, created_at, updated_at)
VALUES (1, TRUE, TRUE, TRUE, TRUE, TRUE, '22:00:00', '07:00:00', NOW(6), NOW(6)),
       (2, TRUE, TRUE, FALSE, TRUE, FALSE, NULL, NULL, NOW(6), NOW(6));

-- ── notification (SAFETY) ─────────────────────────────────────
INSERT INTO notification (type, sent_at, created_at, updated_at)
VALUES ('SAFETY', NOW(6), NOW(6), NOW(6)),   -- id=1
       ('DEVICE', NOW(6), NOW(6), NOW(6)),   -- id=2
       ('GENERAL', NOW(6), NOW(6), NOW(6));  -- id=3

-- ── notification_recipient ────────────────────────────────────
INSERT INTO notification_recipient (notification_id, member_id, is_read, read_at, is_sent, sent_at, created_at,
                                    updated_at)
VALUES (1, 1, FALSE, NULL, TRUE, NOW(6), NOW(6), NOW(6)),
       (2, 1, TRUE, NOW(6), TRUE, NOW(6), NOW(6), NOW(6)),
       (3, 1, FALSE, NULL, TRUE, NOW(6), NOW(6), NOW(6));

-- ── safety_event ──────────────────────────────────────────────
INSERT INTO safety_event (device_id, event_type, severity, confidence, duration_second, detected_at, snapshot_url,
                           video_url, created_at, updated_at)
VALUES (1, 'CRYING', 'CAUTION', 0.9200, 30, NOW(6), NULL, NULL, NOW(6), NOW(6)),
       (1, 'FALL', 'DANGER', 0.9800, 5, NOW(6), NULL, NULL, NOW(6), NOW(6));

-- ── safety_notification ───────────────────────────────────────
INSERT INTO safety_notification (notification_id, safety_event_id, device_id, event_type, severity, created_at,
                                  updated_at)
VALUES (1, 1, 1, 'CRYING', 'CAUTION', NOW(6), NOW(6));

-- ── device_notification ───────────────────────────────────────
INSERT INTO device_notification (notification_id, device_id, component_type, before_status, current_status,
                                  description, created_at, updated_at)
VALUES (2, 2, 'BOARD', 'ONLINE', 'OFFLINE', '연결 끊김', NOW(6), NOW(6));

-- ── general_notification ──────────────────────────────────────
INSERT INTO general_notification (notification_id, detail_type, title, content, created_at, updated_at)
VALUES (3, 'REPORT_AD', '주간 리포트가 생성되었습니다', '이번 주 아이의 수면 리포트를 확인해보세요.', NOW(6), NOW(6));
