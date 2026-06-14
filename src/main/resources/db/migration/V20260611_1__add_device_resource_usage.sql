ALTER TABLE device
    ADD COLUMN cpu_usage    DECIMAL(5, 2) NULL COMMENT 'CPU 사용률 (%)',
    ADD COLUMN memory_usage DECIMAL(5, 2) NULL COMMENT '메모리 사용률 (%)';
