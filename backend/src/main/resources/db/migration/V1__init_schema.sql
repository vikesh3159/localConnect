-- LocalConnect V1 Database Migration
-- Target: MySQL 8.x / H2 MySQL Compatibility Mode

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    preferred_language VARCHAR(10) DEFAULT 'en',
    active_role VARCHAR(30) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    operational_district VARCHAR(100) NOT NULL,
    operational_taluka VARCHAR(100) NOT NULL,
    service_base VARCHAR(150) NOT NULL,
    rating_average DECIMAL(3, 2) DEFAULT 0.00,
    total_completed_jobs INT DEFAULT 0,
    verification_status VARCHAR(30) DEFAULT 'PENDING_APPROVAL',
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_providers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    license_expiry DATE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_drivers_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    sub_type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    registration_number VARCHAR(50) NULL,
    capacity INT NOT NULL DEFAULT 1,
    base_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    pricing_unit VARCHAR(30) NOT NULL DEFAULT 'per_day',
    approval_status VARCHAR(30) DEFAULT 'PENDING_APPROVAL',
    is_active BOOLEAN DEFAULT TRUE,
    opt_lock_version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resources_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS provider_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    driver_id BIGINT NULL,
    resource_id BIGINT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_name VARCHAR(150) NOT NULL,
    verification_status VARCHAR(30) DEFAULT 'PENDING_REVIEW',
    rejection_reason TEXT NULL,
    reviewed_by VARCHAR(100) NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL,
    urgency_level VARCHAR(30) DEFAULT 'NORMAL',
    quantity_required INT NOT NULL DEFAULT 1,
    pickup_location VARCHAR(200) NOT NULL,
    destination_location VARCHAR(200) NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    stay_type VARCHAR(50) NULL,
    goods_type VARCHAR(50) NULL,
    special_instructions TEXT NULL,
    status VARCHAR(30) DEFAULT 'BROADCASTED',
    broadcast_window_hours INT DEFAULT 4,
    broadcast_expires_at TIMESTAMP NOT NULL,
    search_radius_km INT DEFAULT 15,
    opt_lock_version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS request_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    status VARCHAR(30) DEFAULT 'SENT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    viewed_at TIMESTAMP NULL,
    CONSTRAINT fk_inv_request FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS provider_offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    offered_quantity INT NOT NULL,
    quoted_amount DECIMAL(10, 2) NOT NULL,
    includes_driver BOOLEAN NOT NULL DEFAULT TRUE,
    nominated_resource_id BIGINT NULL,
    nominated_driver_id BIGINT NULL,
    status VARCHAR(30) DEFAULT 'SUBMITTED',
    nomination_deadline TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offers_request FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_offers_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    offer_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    driver_id BIGINT NULL,
    allocated_quantity INT NOT NULL,
    agreed_amount DECIMAL(10, 2) NOT NULL,
    confirmation_code VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(30) DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_request FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_offer FOREIGN KEY (offer_id) REFERENCES provider_offers(id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_alloc_provider FOREIGN KEY (provider_id) REFERENCES providers(id),
    CONSTRAINT fk_alloc_resource FOREIGN KEY (resource_id) REFERENCES resources(id)
);

CREATE TABLE IF NOT EXISTS emergency_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    emergency_type VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    caller_location VARCHAR(200) NOT NULL,
    caller_landmark VARCHAR(200) NULL,
    notes TEXT NULL,
    status VARCHAR(40) DEFAULT 'LOCAL_ASSISTANCE_REQUESTED',
    assigned_provider_id BIGINT NULL,
    provider_eta_minutes INT NULL,
    provider_vehicle_plate VARCHAR(50) NULL,
    dialed_helpline VARCHAR(20) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_phone VARCHAR(20) NOT NULL,
    channel VARCHAR(30) DEFAULT 'SMS',
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    delivery_status VARCHAR(30) DEFAULT 'PENDING',
    attempts INT DEFAULT 1,
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    state_before VARCHAR(50) NULL,
    state_after VARCHAR(50) NULL,
    details TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Essential Performance & Foreign Key Indexes
CREATE INDEX idx_requests_status_expires ON service_requests(status, broadcast_expires_at);
CREATE INDEX idx_requests_customer ON service_requests(customer_id);
CREATE INDEX idx_offers_request ON provider_offers(request_id);
CREATE INDEX idx_offers_provider ON provider_offers(provider_id);
CREATE INDEX idx_alloc_request ON booking_allocations(request_id);
CREATE INDEX idx_resources_provider_approval ON resources(provider_id, approval_status);
