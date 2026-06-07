-- ============================================================
-- V20260607_1__seed_data.sql  (local seed)
-- ============================================================

-- ── member ──────────────────────────────────────────────────
INSERT INTO member (name, nickname, email, phone_number, password, role, img_url, created_at, updated_at)
VALUES ('김지민', '지민맘', 'jimin@naranhi.com', '010-1234-5678',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', NULL,
        NOW(6), NOW(6)),
       ('이수현', '수현아빠', 'suhyun@naranhi.com', '010-9876-5432',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', NULL,
        NOW(6), NOW(6));
-- 비밀번호 평문: password

-- ── child ────────────────────────────────────────────────────
INSERT INTO child (member_id, name, birth_date, gender, img_url, created_at, updated_at)
VALUES (1, '김하준', '2024-08-12', 'MALE',   NULL, NOW(6), NOW(6)),
       (2, '이아린', '2025-01-05', 'FEMALE', NULL, NOW(6), NOW(6));

-- ── device ───────────────────────────────────────────────────
INSERT INTO device (device_name, device_serial_number, location_name,
                    board_status, camera_status, mic_status,
                    mqtt_client_id, last_heartbeat_at, last_event_at,
                    created_at, updated_at)
VALUES ('나란히-001', 'SN-2024-0001', '아기방',
        'ONLINE', 'ONLINE', 'ONLINE',
        'mqtt-client-001', NOW(6), NOW(6), NOW(6), NOW(6)),
       ('나란히-002', 'SN-2024-0002', '거실',
        'OFFLINE', 'OFFLINE', 'OFFLINE',
        'mqtt-client-002', NULL, NULL, NOW(6), NOW(6));

-- ── member_device ─────────────────────────────────────────────
INSERT INTO member_device (member_id, device_id, created_at, updated_at)
VALUES (1, 1, NOW(6), NOW(6)),
       (1, 2, NOW(6), NOW(6)),
       (2, 1, NOW(6), NOW(6));

-- ── fcm_token ─────────────────────────────────────────────────
INSERT INTO fcm_token (member_id, token, device_id, platform_type, active, last_used_at, created_at, updated_at)
VALUES (1, 'fcm-test-token-member1-android-000000000000000000000000000000', 'device-uuid-android-001', 'ANDROID',
        TRUE, NOW(6), NOW(6), NOW(6)),
       (2, 'fcm-test-token-member2-android-000000000000000000000000000000', 'device-uuid-android-002', 'ANDROID',
        TRUE, NOW(6), NOW(6), NOW(6));

-- ── notification_setting ──────────────────────────────────────
INSERT INTO notification_setting (member_id,
                                  is_received_safety_notification,
                                  is_received_device_notification,
                                  is_received_report_notification,
                                  is_received_general_notification,
                                  is_interference_active, start_time, interference_end_time,
                                  created_at, updated_at)
VALUES (1, TRUE, TRUE, TRUE, TRUE, TRUE,  '22:00:00', '07:00:00', NOW(6), NOW(6)),
       (2, TRUE, TRUE, TRUE, FALSE, FALSE, NULL,       NULL,       NOW(6), NOW(6));

-- ── safety_event ──────────────────────────────────────────────
-- EventType 기준:
-- PRONE_SUFFOCATION  → DANGER
-- BLANKET_SUFFOCATION → DANGER
-- FALL               → DANGER
-- EXIT               → DANGER
-- CLIMBING           → CAUTION
-- CRYING             → CAUTION

INSERT INTO safety_event (device_id, event_type, severity, confidence,
                          duration_second, detected_at,
                          snapshot_url, video_url, created_at, updated_at)
VALUES
    -- PRONE_SUFFOCATION (위험)
    (1, 'PRONE_SUFFOCATION',   'DANGER',  0.9600, 8,  NOW(6) - INTERVAL 10 MINUTE, NULL, NULL, NOW(6), NOW(6)),
    -- BLANKET_SUFFOCATION (위험)
    (1, 'BLANKET_SUFFOCATION', 'DANGER',  0.9100, 12, NOW(6) - INTERVAL 30 MINUTE, NULL, NULL, NOW(6), NOW(6)),
    -- FALL (위험)
    (1, 'FALL',                'DANGER',  0.9800, 3,  NOW(6) - INTERVAL 1 HOUR,    NULL, NULL, NOW(6), NOW(6)),
    -- EXIT (위험)
    (1, 'EXIT',                'DANGER',  0.8800, 5,  NOW(6) - INTERVAL 2 HOUR,    NULL, NULL, NOW(6), NOW(6)),
    -- CLIMBING (주의)
    (1, 'CLIMBING',            'CAUTION', 0.8500, 15, NOW(6) - INTERVAL 3 HOUR,    NULL, NULL, NOW(6), NOW(6)),
    -- CRYING (주의)
    (1, 'CRYING',              'CAUTION', 0.9200, 180,NOW(6) - INTERVAL 4 HOUR,    NULL, NULL, NOW(6), NOW(6));

-- ── notification ──────────────────────────────────────────────
INSERT INTO notification (type, sent_at, created_at, updated_at)
VALUES ('SAFETY',  NOW(6) - INTERVAL 10 MINUTE, NOW(6), NOW(6)),  -- id=1 PRONE_SUFFOCATION
       ('SAFETY',  NOW(6) - INTERVAL 30 MINUTE, NOW(6), NOW(6)),  -- id=2 BLANKET_SUFFOCATION
       ('SAFETY',  NOW(6) - INTERVAL 1 HOUR,    NOW(6), NOW(6)),  -- id=3 FALL
       ('SAFETY',  NOW(6) - INTERVAL 2 HOUR,    NOW(6), NOW(6)),  -- id=4 EXIT
       ('SAFETY',  NOW(6) - INTERVAL 3 HOUR,    NOW(6), NOW(6)),  -- id=5 CLIMBING
       ('SAFETY',  NOW(6) - INTERVAL 4 HOUR,    NOW(6), NOW(6)),  -- id=6 CRYING
       ('DEVICE',  NOW(6) - INTERVAL 5 HOUR,    NOW(6), NOW(6)),  -- id=7 장치 오프라인
       ('GENERAL', NOW(6) - INTERVAL 1 DAY,     NOW(6), NOW(6));  -- id=8 리포트

-- ── safety_notification ───────────────────────────────────────
INSERT INTO safety_notification (notification_id, safety_event_id, device_id,
                                 event_type, severity, created_at, updated_at)
VALUES (1, 1, 1, 'PRONE_SUFFOCATION',   'DANGER',  NOW(6), NOW(6)),
       (2, 2, 1, 'BLANKET_SUFFOCATION', 'DANGER',  NOW(6), NOW(6)),
       (3, 3, 1, 'FALL',                'DANGER',  NOW(6), NOW(6)),
       (4, 4, 1, 'EXIT',                'DANGER',  NOW(6), NOW(6)),
       (5, 5, 1, 'CLIMBING',            'CAUTION', NOW(6), NOW(6)),
       (6, 6, 1, 'CRYING',              'CAUTION', NOW(6), NOW(6));

-- ── device_notification ───────────────────────────────────────
INSERT INTO device_notification (notification_id, device_id, component_type,
                                 before_status, current_status, description,
                                 created_at, updated_at)
VALUES (7, 2, 'BOARD', 'ONLINE', 'OFFLINE', '연결 끊김', NOW(6), NOW(6));

-- ── general_notification ──────────────────────────────────────
INSERT INTO general_notification (notification_id, detail_type, title, content,
                                  created_at, updated_at)
VALUES (8, 'REPORT', '주간 리포트가 생성되었습니다',
        '이번 주 총 6건의 이벤트가 감지되었습니다. 자세한 내용을 확인해보세요.',
        NOW(6), NOW(6));

-- ── notification_recipient ────────────────────────────────────
INSERT INTO notification_recipient (notification_id, member_id,
                                    is_read, read_at,
                                    is_sent, sent_at, sent_fail_reason,
                                    created_at, updated_at)
VALUES
    -- member 1 수신 (안읽음)
    (1, 1, FALSE, NULL, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (2, 1, FALSE, NULL, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (3, 1, TRUE,  NOW(6) - INTERVAL 50 MINUTE, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (4, 1, TRUE,  NOW(6) - INTERVAL 1 HOUR,    TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (5, 1, TRUE,  NOW(6) - INTERVAL 2 HOUR,    TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (6, 1, TRUE,  NOW(6) - INTERVAL 3 HOUR,    TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (7, 1, FALSE, NULL, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (8, 1, FALSE, NULL, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    -- member 2 수신 (device 1 공유)
    (1, 2, FALSE, NULL, TRUE, NOW(6), NULL, NOW(6), NOW(6)),
    (2, 2, TRUE,  NOW(6) - INTERVAL 25 MINUTE, TRUE, NOW(6), NULL, NOW(6), NOW(6));