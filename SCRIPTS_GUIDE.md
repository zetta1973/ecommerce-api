# 🚀 Guía de Scripts de Despliegue - Ecommerce API

## 📋 Tabla de Contenidos
- [Scripts Disponibles](#scripts-disponibles)
- [Flujos de Despliegue](#flujos-de-despliegue)
- [Configuración y Requisitos](#configuración-y-requisitos)
- [Comandos Útiles](#comandos-útiles)
- [Solución de Problemas](#solución-de-problemas)

---

## 🛠️ Scripts Disponibles

### 1. **deploy-local.sh** ⭐ (Script Principal)
```bash
./scripts/deploy-local.sh [tag]
```
- **Propósito**: Despliega la aplicación en Kubernetes local (Rancher Desktop)
- **Uso principal**: Cargar imagen generada de ecommerce al cluster local
- **Imagen por defecto**: `ghcr.io/zetta1973/ecommerce-api:ci-latest`
- **Argumentos opcionales**: Tag específico de imagen

**Características:**
- ✅ Actualiza automáticamente la imagen en `deployment.yaml`
- ✅ Espera a que los pods estén listos
- ✅ Verifica conexión al cluster
- ✅ Crea namespace si no existe
- ✅ Muestra comandos de acceso

### 2. **deploy-all.sh** 🌟 (Despliegue Completo)
```bash
./scripts/deploy-all.sh [tag]
```
- **Propósito**: Despliega toda la infraestructura + aplicación
- **Componentes**: PostgreSQL, Kafka, Jenkins, Kafka UI + Ecommerce API
- **Ideal**: Primer despliegue o entorno completo

### 3. **deploy-infrastructure.sh** 🏗️ (Solo Infraestructura)
```bash
./scripts/deploy-infrastructure.sh
```
- **Propósito**: Despliega solo la infraestructura
- **Componentes**: PostgreSQL, Kafka, Jenkins, Kafka UI
- **No incluye**: Ecommerce API

---

## 🔄 Flujos de Despliegue

### 🥇 Primer Despliegue (Recomendado)
```bash
# Desplegar todo en un solo comando
./scripts/deploy-all.sh

# O paso a paso:
./scripts/deploy-infrastructure.sh
./scripts/deploy-local.sh
```

### 🔄 Actualizaciones Posteriores
```bash
# Solo actualizar la aplicación (recomendado)
./scripts/deploy-local.sh

# Con tag específico
./scripts/deploy-local.sh 1.3.1
```

### 🎯 Flujo Específico por Commit
```bash
# Usar un commit específico
./scripts/deploy-local.sh a1b2c3d4e5f6
```

---

## ⚙️ Configuración y Requisitos

### Prerrequisitos
- ✅ Rancher Desktop instalado y ejecutándose
- ✅ `kubectl` configurado y conectado
- ✅ Docker (o container runtime) disponible
- ✅ Imagen `ghcr.io/zetta1973/ecommerce-api:ci-latest` disponible

### Variables de Entorno
```bash
NAMESPACE="ecommerce"
IMAGE_REPO="ghcr.io/zetta1973/ecommerce-api"
IMAGE_TAG="ci-latest"  # o tag específico
```

### Configuración de Kubernetes
- **Namespace**: `ecommerce`
- **Servicios**: PostgreSQL (5432), Kafka (9092)
- **Accesos**: Port-forward para servicios externos

---

## 🎮 Comandos Útiles

### Acceder a la API
```bash
kubectl port-forward svc/ecommerce-api-service 8080:80 -n ecommerce
```
- **URL local**: http://localhost:8080
- **Health check**: http://localhost:8080/actuator/health

### Acceder a Otros Servicios
```bash
# Jenkins
kubectl port-forward svc/jenkins 8080:8080 -n ecommerce

# Kafka UI
kubectl port-forward svc/kafka-ui 8080:8080 -n ecommerce
```

### Verificar Estado
```bash
# Ver todos los pods
kubectl get pods -n ecommerce

# Ver pods de la aplicación
kubectl get pods -n ecommerce -l app=ecommerce-api

# Ver logs de la aplicación
kubectl logs -f deployment/ecommerce-api -n ecommerce

# Ver rollout status
kubectl rollout status deployment/ecommerce-api -n ecommerce
```

### Limpieza
```bash
# Limpiar solo aplicación
kubectl delete deployment ecommerce-api -n ecommerce

# Limpiar todo (cuidado)
kubectl delete namespace ecommerce
```

---

## 🔧 Scripts de Mantenimiento

### Scripts de Limpieza
```bash
./scripts/cleanup-local.sh      # Limpia despliegue local
./scripts/clean-deploy.sh      # Limpia y redeploya
./scripts/rollback-local.sh     # Rollback de versión
```

### Scripts de Monitoreo
```bash
./scripts/logs-local.sh        # Ver logs de aplicación
```

---

## 🎯 Endpoints de la API (Después de Despliegue)

### 🟢 Públicos (sin autenticación)
```http
GET  http://localhost:8080/admin/ping
POST http://localhost:8080/auth/register
POST http://localhost:8080/auth/login
GET  http://localhost:8080/api/products
GET  http://localhost:8080/api/products/search?name=producto
```

### 🔐 Protegidos (requieren token JWT)
```http
GET  http://localhost:8080/admin/users
POST http://localhost:8080/api/products
PUT  http://localhost:8080/api/products/{id}
GET  http://localhost:8080/api/products/{id}
GET  http://localhost:8080/api/products/{id}/stock
GET  http://localhost:8080/orders
POST http://localhost:8080/orders
POST http://localhost:8080/admin/roles/assign
```

---

## 🐛 Solución de Problemas

### Problemas Comunes

#### 1. **kubectl no encontrado**
```bash
# Asegúrate de tener Rancher Desktop ejecutándose
kubectl version
```

#### 2. **Error de conexión al cluster**
```bash
kubectl cluster-info
# Si falla, reinicia Rancher Desktop
```

#### 3. **Imagen no encontrada**
```bash
# Verificar que la imagen existe localmente
docker images | grep ecommerce-api
# O descargarla
docker pull ghcr.io/zetta1973/ecommerce-api:ci-latest
```

#### 4. **Pods no inician**
```bash
# Verificar estado detallado
kubectl describe pods -n ecommerce
kubectl logs -f deployment/ecommerce-api -n ecommerce
```

#### 5. **Namespace no existe**
```bash
# Crear manualmente
kubectl create namespace ecommerce
```

### Debugging Avanzado
```bash
# Ver eventos del namespace
kubectl get events -n ecommerce --sort-by='.lastTimestamp'

# Ver recursos desplegados
kubectl get all -n ecommerce

# Forzar reinicio
kubectl rollout restart deployment/ecommerce-api -n ecommerce
```

---

## 📊 Resumen Rápido

| Situación | Script a Usar |
|-----------|---------------|
| 🥇 Primer despliegue | `./scripts/deploy-all.sh` |
| 🔄 Actualización de API | `./scripts/deploy-local.sh` |
| 🏗️ Solo infraestructura | `./scripts/deploy-infrastructure.sh` |
| 🧹 Limpieza completa | `./scripts/cleanup-local.sh` |
| 📋 Ver logs | `./scripts/logs-local.sh` |
| 🔙 Rollback | `./scripts/rollback-local.sh` |

---

## 🚀 Flujo de Trabajo Típico

### Desarrollo Local
```bash
# 1. Hacer cambios en el código
# 2. Subir a GitHub (CI genera la imagen)
# 3. Desplegar actualización
./scripts/deploy-local.sh

# 4. Probar la API
kubectl port-forward svc/ecommerce-api-service 8080:80 -n ecommerce
curl http://localhost:8080/admin/ping
```

### Producción/Staging
```bash
# 1. Usar tag específico
./scripts/deploy-local.sh v1.3.1

# 2. Verificar despliegue
kubectl get pods -n ecommerce -l app=ecommerce-api
kubectl rollout status deployment/ecommerce-api -n ecommerce
```

---

*Última actualización: Enero 2026*
*Versión: 1.0*