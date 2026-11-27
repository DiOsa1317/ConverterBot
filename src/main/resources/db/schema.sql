CREATE TABLE IF NOT EXISTS custom_conversions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    from_unit VARCHAR(64) NOT NULL,
    to_unit VARCHAR(64) NOT NULL,
    rate DECIMAL(20, 10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_custom_conv UNIQUE (chat_id, from_unit, to_unit)
);

CREATE TABLE IF NOT EXISTS conversion_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    input VARCHAR(255) NOT NULL,
    result VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);