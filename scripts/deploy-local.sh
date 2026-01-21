#!/bin/bash

# ============================================
# Ecommerce API - Despliegue Local Completo
# ============================================
# Uso: bash scripts/deploy-local.sh
# 
# Este script:
# 1. Crea un cluster Kind (si no existe)
# 2. Despliega PostgreSQL y Kafka
# 3. Construye la imagen Docker local
# 4. Despliega la aplicación en Kubernetes
# 5. Configura port-forwarding
# 6. Ejecuta pruebas básicas
# ============================================

set -e

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

NAMESPACE="ecommerce"
IMAGE_NAME="ecommerce-api:local"

# Función para mostrar mensajes
echo_message() {
    local color=$1
    local message=$2
    echo -e "${color}[*] ${message}${NC}"
}

# Función para verificar si un comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verificar prerequisitos
echo_message "$YELLOW" "Verificando prerequisitos..."

if ! command_exists kind; then
    echo_message "$RED" "Kind no está instalado. Instalando..."
    curl -Lo kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64
    chmod +x kind
    sudo mv kind /usr/local/bin/
    echo_message "$GREEN" "Kind instalado"
fi

if ! command_exists kubectl; then
    echo_message "$RED" "kubectl no está instalado"
    exit 1
fi

if ! command_exists docker; then
    echo_message "$RED" "Docker no está instalado"
    exit 1
fi

if ! command_exists mvn; then
    echo_message "$RED" "Maven no está instalado"
    exit 1
fi

echo_message "$GREEN" "Todos los prerequisitos están instalados"
echo ""

# ============================================
# 1. Crear cluster Kind
# ============================================
echo_message "$BLUE" "Creando cluster Kind..."

if ! kind get clusters | grep -q "$NAMESPACE"; then
    echo_message "$YELLOW" "Cluster no encontrado. Creando nuevo cluster..."
    kind create cluster --name $NAMESPACE
    echo_message "$GREEN" "Cluster creado"
else
    echo_message "$YELLOW" "Cluster ya existe. Reutilizando..."
fi

echo ""

# ============================================
# 2. Desplegar infraestructura (PostgreSQL + Kafka)
# ============================================
echo_message "$BLUE" "Desplegando infraestructura..."

# Crear namespace
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Desplegar PostgreSQL y Kafka
kubectl apply -f k8s/infrastructure/ -n $NAMESPACE

echo_message "$YELLOW" "Esperando a que PostgreSQL esté listo..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=180s -n $NAMESPACE

echo_message "$YELLOW" "Esperando a que Kafka esté listo..."
kubectl wait --for=condition=ready pod -l app=kafka --timeout=240s -n $NAMESPACE

echo_message "$GREEN" "Infraestructura desplegada"
echo ""

# ============================================
# 3. Construir imagen Docker
# ============================================
echo_message "$BLUE" "Construyendo imagen Docker..."

# Compilar el proyecto
mvn clean package -DskipTests -q

# Construir imagen Docker
docker build -t $IMAGE_NAME .

# Verificar imagen
docker images | grep $IMAGE_NAME > /dev/null && \
    echo_message "$GREEN" "Imagen Docker construida: $IMAGE_NAME" || \
    (echo_message "$RED" "Error al construir imagen" && exit 1)

echo ""

# ============================================
# 4. Cargar imagen en Kind y desplegar aplicación
# ============================================
echo_message "$BLUE" "Cargando imagen en Kind y desplegando aplicación..."

# Cargar imagen en Kind
kind load docker-image $IMAGE_NAME --name $NAMESPACE

# Desplegar secrets
echo_message "$YELLOW" "Desplegando secrets..."
kubectl apply -f k8s/application/secrets.yaml -n $NAMESPACE

# Desplegar aplicación
kubectl apply -f k8s/application/ -n $NAMESPACE

echo_message "$YELLOW" "Esperando a que la aplicación esté lista..."
kubectl wait --for=condition=ready pod -l app=ecommerce-api --timeout=180s -n $NAMESPACE

echo_message "$GREEN" "Aplicación desplegada"
echo ""

# ============================================
# 5. Configurar port-forwarding
# ============================================
echo_message "$BLUE" "Configurando port-forwarding..."

# Obtener nombre del pod
POD_NAME=$(kubectl get pods -l app=ecommerce-api -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}')

# Iniciar port-forwarding en segundo plano
kubectl port-forward pod/$POD_NAME 8080:8080 -n $NAMESPACE > /dev/null 2>&1 &
PORT_FORWARD_PID=$!

echo_message "$GREEN" "Port-forwarding configurado (puedo: 8080)"
echo ""

# ============================================
# 6. Ejecutar pruebas básicas
# ============================================
echo_message "$BLUE" "Ejecutando pruebas básicas..."

# Esperar un momento para que la aplicación esté lista
sleep 10

# Probar health endpoint
if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo_message "$GREEN" "✓ Health check: PASSED"
else
    echo_message "$RED" "✗ Health check: FAILED"
    echo "Intentando obtener logs..."
    kubectl logs -l app=ecommerce-api -n $NAMESPACE --tail=50
    exit 1
fi

# Probar endpoint público
if curl -sf http://localhost:8080/api/products > /dev/null 2>&1; then
    echo_message "$GREEN" "✓ API products: PASSED"
else
    echo_message "$RED" "✗ API products: FAILED"
    exit 1
fi

echo ""
echo_message "$GREEN" "Todas las pruebas básicas PASSED"
echo ""

# ============================================
# Resumen
# ============================================
echo "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo "${GREEN}✓ Despliegue local completado con éxito!${NC}"
echo "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo "📋 Información del despliegue:"
echo "  - Namespace: $NAMESPACE"
echo "  - Imagen: $IMAGE_NAME"
echo "  - URL: http://localhost:8080"
echo "  - Port-forwarding PID: $PORT_FORWARD_PID"
echo ""
echo "🔧 Comandos útiles:"
echo "  - Ver pods: kubectl get pods -n $NAMESPACE"
echo "  - Ver logs: kubectl logs -l app=ecommerce-api -n $NAMESPACE -f"
echo "  - Ver servicios: kubectl get svc -n $NAMESPACE"
echo "  - Eliminar cluster: kind delete cluster --name $NAMESPACE"
echo "  - Parar port-forwarding: kill $PORT_FORWARD_PID"
echo ""
echo "🚀 ¡La aplicación está lista para usar!"
