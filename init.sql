-- Crear tabla role
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Crear tabla permission
CREATE TABLE IF NOT EXISTS permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Crear tabla role_permissions
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permissions_id INT NOT NULL,  -- ← Nombre correcto según tu estructura
    PRIMARY KEY (role_id, permissions_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permissions_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- Crear tabla users (cambiamos de "user" a "users" para evitar palabra reservada)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- Crear tabla product
CREATE TABLE IF NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    stock INT NOT NULL
);

-- Crear tabla orders (cambiamos de "order" a "orders" para evitar palabra reservada)
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INT,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Crear tabla order_products
CREATE TABLE IF NOT EXISTS order_products (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Insertar roles base
INSERT INTO role (name) VALUES ('USER'), ('ADMIN') ON CONFLICT DO NOTHING;

-- Insertar permisos base
INSERT INTO permission (name) VALUES
    ('READ_OWN_PROFILE'),
    ('UPDATE_OWN_PROFILE'),
    ('READ_OWN_ORDERS'),
    ('CREATE_ORDERS'),
    ('READ_PRODUCTS'),
    ('CREATE_PRODUCTS'),
    ('UPDATE_PRODUCTS'),
    ('DELETE_PRODUCTS'),
    ('READ_PRODUCT_STOCK'),
    ('READ_USERS'),
    ('UPDATE_USERS'),
    ('DELETE_USERS'),
    ('READ_USER_ORDERS'),
    ('READ_ALL_ORDERS'),
    ('UPDATE_ORDER_STATUS'),
    ('DELETE_ORDERS'),
    ('MANAGE_ROLES'),
    ('MANAGE_PERMISSIONS'),
    ('READ_AUDIT_LOGS'),
    ('READ_STATISTICS'),
    ('READ_REPORTS')
ON CONFLICT DO NOTHING;

-- Asignar permisos a roles
-- Rol USER
INSERT INTO role_permissions (role_id, permissions_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'USER'
  AND p.name IN (
    'READ_OWN_PROFILE',
    'UPDATE_OWN_PROFILE',
    'READ_OWN_ORDERS',
    'CREATE_ORDERS',
    'READ_PRODUCTS'
  )
ON CONFLICT DO NOTHING;

-- Rol ADMIN
INSERT INTO role_permissions (role_id, permissions_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN'
  AND p.name IN (
    'READ_OWN_PROFILE',
    'UPDATE_OWN_PROFILE',
    'READ_OWN_ORDERS',
    'CREATE_ORDERS',
    'READ_PRODUCTS',
    'CREATE_PRODUCTS',
    'UPDATE_PRODUCTS',
    'DELETE_PRODUCTS',
    'READ_USER_ORDERS',
    'READ_USERS',
    'UPDATE_USERS',
    'DELETE_USERS',
    'READ_ALL_ORDERS',
    'UPDATE_ORDER_STATUS',
    'DELETE_ORDERS',
    'MANAGE_ROLES',
    'MANAGE_PERMISSIONS',
    'READ_AUDIT_LOGS',
    'READ_STATISTICS',
    'READ_REPORTS'
  )
ON CONFLICT DO NOTHING;

-- Insertar un usuario de ejemplo
INSERT INTO users (username, email, password, role_id)
VALUES (
    'admin',
    'admin@test.com',
    '$2a$10$vDtCxgJ5DlII7pqqqRIF6OpEs8Ez.qW9F8j2kr3LjnwNyqGXvJQ4G',  -- contraseña: "mipass123"
    (SELECT id FROM role WHERE name = 'ADMIN')
)
ON CONFLICT DO NOTHING;

INSERT INTO users (username, email, password, role_id)
VALUES (
    'user',
    'user@test.com',
    '$2a$10$vDtCxgJ5DlII7pqqqRIF6OpEs8Ez.qW9F8j2kr3LjnwNyqGXvJQ4G',  -- contraseña: "mipass123"
    (SELECT id FROM role WHERE name = 'USER')
)
ON CONFLICT DO NOTHING;
