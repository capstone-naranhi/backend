ALTER TABLE device
    ADD COLUMN cpu_usage    DOUBLE NULL COMMENT 'CPU 사용률 (%)',
    ADD COLUMN memory_usage DOUBLE NULL COMMENT '메모리 사용률 (%)';
