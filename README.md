# 🛍️ Ecommerce API

Plataforma de comercio electrónico construida con Java + Spring Boot, siguiendo la filosofía API-first con OpenAPI 3.0. Totalmente dockerizada y lista para CI/CD con Jenkins.

## 🚀 Funcionalidades

- Registro y login de usuarios (JWT)
- Gestión de productos
- Carrito de compra
- Creación y seguimiento de pedidos
- Valoraciones de productos
- Simulación de pagos

## 🧱 Tecnologías

- Java 17 + Spring Boot
- PostgreSQL
- OpenAPI 3.0 + Swagger UI
- Docker + Docker Compose
- Jenkins (CI/CD)
- Maven

## 📦 Cómo ejecutar en local

```bash
docker-compose up --build


{
  "roleName": "ADMIN",
  "permissionName": "CREATE_PRODUCTS"
}

JENKINS ADMIN PASSWORD GENERADO 6bc2c01f66ab4a35bf3bbdaf75d9e1f1

docker login
docker build -t ecommerce-api-1.0.1 . # subir la imagen a rancher

docker ps -a -f "name=ecommerce" --format "table {{.ID}}\t{{.Image}}\t{{.Names}}"
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}"

kubectl -n ecommerce get pods -l app=ecommerce-api
kubectl -n ecommerce logs -l app=ecommerce-api -f

RECONSTRUIR LA IMAGEN
docker build -t ecommerce-api:1.0.1 .


kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=TU_USUARIO/ecommerce-api:1.0.3

kubectl -n ecommerce get pods -l app=ecommerce-api
kubectl -n ecommerce logs -l app=ecommerce-api -f

# Describe el pod para ver eventos
kubectl -n ecommerce describe pod -l app=ecommerce-api

# Verifica que esté usando la imagen correcta
kubectl -n ecommerce describe pod -l app=ecommerce-api | grep -i image

#Actualizar la imagen
kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=ecommerce-api:1.0.4

 Resumen de pasos completos
1. Asegúrate de tener la nueva SecurityConfig
✅ Edita
SecurityConfig.java
2. Reconstruye la imagen
docker build -t ecommerce-api:1.0.4 .
3. Etiqueta para el registry de Rancher
docker tag ecommerce-api:1.0.4 rancher.local/ecommerce/ecommerce-api:1.0.4
4. Sube al registry
docker push rancher.local/ecommerce/ecommerce-api:1.0.4
5. Actualiza el Deployment
kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=rancher.local/ecommerce/ecommerce-api:1.0.4
6. Despausa el Deployment
kubectl -n ecommerce rollout resume deployment/ecommerce-api
7. Prueba
curl http://localhost:30080/admin/ping


docker images | grep ecommerce
kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=ecommerce-api:1.0.4


# 1. Reconstruye la imagen
docker build -t ecommerce-api:1.0.4 .

# 2. Verifica
docker images | grep ecommerce

# 3. Actualiza el Deployment
kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=ecommerce-api:1.0.4

# 4. Despausa
kubectl -n ecommerce rollout resume deployment/ecommerce-api

# 5. Espera que el nuevo pod esté listo
kubectl -n ecommerce get pods -w

kubectl -n ecommerce get pods -l app=ecommerce-api
kubectl -n ecommerce describe deployment ecommerce-api | grep -i image
kubectl -n ecommerce describe pod -l app=ecommerce-api | grep -i image

kubectl -n ecommerce get deployment ecommerce-api

Resumen de comandos para diagnóstico
Ver estructura tabla
user
kubectl -n ecommerce exec -it deploy/postgres -- psql -U user -d ecommerce -c "\\d \"user\""
Ver datos de la tabla
kubectl -n ecommerce exec -it deploy/postgres -- psql -U user -d ecommerce -c "SELECT * FROM \"user\";"
Ver tablas en la BD
kubectl -n ecommerce exec -it deploy/postgres -- psql -U user -d ecommerce -c "\\dt"


./mvnw clean package
docker build -t ecommerce-api:1.0.4 .
kubectl -n ecommerce rollout restart deployment/ecommerce-api


# Compila de nuevo
./mvnw clean package

# Verifica el JAR
ls -la target/

# Limpia imágenes antiguas (opcional)
docker rmi ecommerce-api:1.0.6 2>/dev/null || true
# Reconstruye
docker build -t ecommerce-api:1.0.7 .
# actualiza imagen 
kubectl -n ecommerce set image deployment/ecommerce-api ecommerce-api=ecommerce-api:1.0.7
# Forzar reinicio
kubectl -n ecommerce rollout restart deployment/ecommerce-api


-- Asegura que hay un rol
INSERT INTO role (name) VALUES ('USER') ON CONFLICT DO NOTHING;

-- Actualiza el usuario para que tenga un rol válido
UPDATE users SET role_id = 1 WHERE email = 'zeta@test.com';


-- Crear una secuencia para role
CREATE SEQUENCE role_id_seq;

-- Actualizar la columna id para que use la secuencia
ALTER TABLE role ALTER COLUMN id SET DEFAULT nextval('role_id_seq');
ALTER SEQUENCE role_id_seq OWNED BY role.id;

-- Asignar valores actuales si hay datos
SELECT setval('role_id_seq', COALESCE((SELECT MAX(id)+1 FROM role), 1), false);

-- Crear secuencia
CREATE SEQUENCE IF NOT EXISTS role_id_seq;

-- Actualizar la columna id
ALTER TABLE role ALTER COLUMN id SET DEFAULT nextval('role_id_seq');
ALTER SEQUENCE role_id_seq OWNED BY role.id;

-- Asignar valores si es necesario
SELECT setval('role_id_seq', COALESCE((SELECT MAX(id)+1 FROM role), 1), false);

-- Insertar roles si no existen
INSERT INTO role (name) VALUES ('USER') ON CONFLICT DO NOTHING;
INSERT INTO role (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;

-- Verificar que todo esté bien
SELECT * FROM role;


kubectl -n ecommerce get svc kafka-ui
