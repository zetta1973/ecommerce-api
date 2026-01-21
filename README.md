# 🛍️ Ecommerce API

Plataforma de comercio electrónico construida con Java + Spring Boot, siguiendo la filosofía API-first con OpenAPI 3.0. Totalmente dockerizada y lista para CI/CD con GitHub Actions.

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
- GitHub Actions (CI/CD)
- Maven
- Kubernetes (Kind/Rancher Desktop)

## 📦 Cómo ejecutar en local

### Opción 1: Despliegue rápido con script (recomendado)

```bash
# Despliega todo en un solo comando
bash scripts/deploy-local.sh
```

Este script:
- Crea cluster Kind
- Despliega PostgreSQL y Kafka
- Construye imagen Docker
- Despliega aplicación
- Configura port-forwarding a localhost:8080

### Opción 2: Ejecución manual

```bash
# Compilar y ejecutar
mvn spring-boot:run

# Acceder a la API
curl http://localhost:8080/api/products
```

### Opción 3: Docker Compose

```bash
# Ejecutar con Docker Compose
docker-compose up --build
```

## 🔧 Despliegue en Kubernetes

### Configurar cluster

```bash
# Configurar cluster Kind
bash scripts/setup-kind.sh
```

### Desplegar infraestructura

```bash
# Desplegar PostgreSQL y Kafka
kubectl apply -f k8s/infrastructure/ -n ecommerce

# Verificar que estén listos
kubectl get pods -n ecommerce
```

### Desplegar aplicación

```bash
# Desplegar la aplicación
kubectl apply -f k8s/application/ -n ecommerce

# Ver pods
kubectl get pods -n ecommerce

# Ver logs
kubectl logs -l app=ecommerce-api -n ecommerce -f
```

## 🚀 Despliegue desde GHCR

```bash
# Desplegar última versión desde GitHub Container Registry
bash scripts/auto-deploy-ghcr.sh ecommerce
```

## 🧪 Pruebas

### Tests unitarios

```bash
# Ejecutar tests
mvn test

# Generar reporte de cobertura
mvn jacoco:report
```

### Pruebas de API

```bash
# Pruebas básicas con curl
bash scripts/test-api.sh

# Pruebas completas con flujo de usuario
bash scripts/run-api-tests.sh
```

## 📋 Comandos útiles

```bash
# Ver pods
kubectl get pods -n ecommerce

# Ver servicios
kubectl get svc -n ecommerce

# Ver logs
kubectl logs -l app=ecommerce-api -n ecommerce -f

# Port-forwarding
kubectl port-forward svc/ecommerce-api 8080:8080 -n ecommerce

# Eliminar cluster
kind delete cluster --name ecommerce
```

## 📊 Arquitectura

```
┌───────────────────────────────────────────────────────┐
│                   Kubernetes Cluster                  │
├───────────────────┬───────────────────┬─────────────────┤
│   PostgreSQL      │   Kafka          │ Ecommerce API   │
│  (persistence)    │  (events)        │  (REST API)     │
└───────────────────┴───────────────────┴─────────────────┘
```

## 📁 Estructura del proyecto

```
.
├── k8s/
│   ├── application/        # Manifestos de la aplicación
│   ├── infrastructure/     # PostgreSQL y Kafka
│   └── overlays/           # Configuraciones para diferentes entornos
├── scripts/                # Scripts útiles
│   ├── deploy-local.sh     # Despliegue local completo
│   ├── setup-kind.sh       # Configurar cluster Kind
│   ├── auto-deploy-ghcr.sh # Desplegar desde GHCR
│   └── test-api.sh         # Pruebas de API
├── src/
│   ├── main/
│   │   ├── java/           # Código fuente
│   │   └── resources/      # Configuración
│   └── test/              # Tests
└── pom.xml                # Dependencias Maven
```

## 🔐 Autenticación

### Endpoints públicos

- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Login y obtener JWT
- `POST /auth/refresh` - Refrescar token
- `GET /api/products` - Listar productos

### Endpoints protegidos

Todos los endpoints bajo `/api/` y `/admin/` requieren:
```
Authorization: Bearer <JWT_TOKEN>
```

## 📚 Documentación

- [API ENDPOINTS.md](API_ENDPOINTS.md) - Documentación detallada de endpoints
- [CI-CD-SETUP.md](CI-CD-SETUP.md) - Configuración de CI/CD
- [README-PROCESS.md](README-PROCESS.md) - Proceso de desarrollo

## 🤝 Contribución

1. Fork el repositorio
2. Crea una branch con tu feature: `git checkout -b feature/nueva-funcionalidad`
3. Haz commit de tus cambios: `git commit -m 'feat: añadir nueva funcionalidad'`
4. Push a la branch: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

## 📄 Licencia

MIT
