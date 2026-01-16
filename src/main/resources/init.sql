-- Crear tabla de permisos
CREATE TABLE permission (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) UNIQUE NOT NULL
);

-- Crear tabla de roles
CREATE TABLE role (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) UNIQUE NOT NULL
);

-- Tabla intermedia role_permission
CREATE TABLE role_permissions (
  role_id INTEGER REFERENCES role(id),
  permission_id INTEGER REFERENCES permission(id),
  PRIMARY KEY (role_id, permission_id)
);

-- Insertar permisos
INSERT INTO permission (name) VALUES
  ('READ_USERS'),
  ('CREATE_PRODUCTS'),
  ('DELETE_PRODUCTS'),
  ('VIEW_ORDERS'),
  ('MANAGE_ORDERS');

-- Insertar roles
INSERT INTO role (name) VALUES
  ('ADMIN'),
  ('USER');

-- Asignar permisos a ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN';

-- USER sin permisos por defecto
