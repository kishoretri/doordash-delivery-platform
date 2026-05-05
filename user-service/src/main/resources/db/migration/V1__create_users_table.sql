-- Create roles table
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create join table between users and roles
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL REFERENCES users(id),
    role_id     BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Insert the 4 default roles
INSERT INTO roles (name) VALUES
    ('ROLE_CUSTOMER'),
    ('ROLE_DRIVER'),
    ('ROLE_RESTAURANT'),
    ('ROLE_ADMIN');