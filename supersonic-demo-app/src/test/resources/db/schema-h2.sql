-- SuperSonic Demo H2 database schema

-- Model table
CREATE TABLE IF NOT EXISTS s2_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biz_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Dimension table
CREATE TABLE IF NOT EXISTS s2_dimension (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    biz_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    type VARCHAR(50) NOT NULL DEFAULT 'categorical',
    alias VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_dim_model_id ON s2_dimension(model_id);

-- Metric table
CREATE TABLE IF NOT EXISTS s2_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    biz_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    type VARCHAR(50) NOT NULL DEFAULT 'ATOMIC',
    alias VARCHAR(500),
    default_agg VARCHAR(50) DEFAULT 'SUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_metric_model_id ON s2_metric(model_id);

-- Data set table
CREATE TABLE IF NOT EXISTS s2_data_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biz_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    alias VARCHAR(500),
    model_ids VARCHAR(500) NOT NULL,
    dimension_ids VARCHAR(2000),
    metric_ids VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Dimension value table
CREATE TABLE IF NOT EXISTS s2_dimension_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dimension_id BIGINT NOT NULL,
    value VARCHAR(500) NOT NULL,
    alias VARCHAR(500),
    frequency INT DEFAULT 100000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dimval_dimension_id ON s2_dimension_value(dimension_id);
