#!/bin/bash

# ============================================
# Ecommerce API - Despliegue en Rancher Desktop
# ============================================
# Uso: bash scripts/deploy-rancher.sh
# 
# Este script:
# 1. Verifica prerequisitos
# 2. Crea namespace si no existe
# 3. Despliega PostgreSQL y Kafka
# 4. Despliega la aplicación desde GHCR
# 5. Configura port-forwarding
# ============================================

set -e

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

NAMESPACE="ecommerce"

# Función para mostrar mensajes
echo_message() {
    local color=$1
    local message=$2
    echo -e "${color}[*] ${message}${NC}"
}

# Verificar prerequisitos
echo_message "$YELLOW" "Verificando prerequisitos..."

if ! command -v kubectl >/dev/null 2>&1; then
    echo_message "$RED" "kubectl no está instalado"
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo_message "$RED" "Docker no está instalado"
    exit 1
fi

echo_message "$GREEN" "Todos los prerequisitos están instalados"
echo ""

# ============================================
# 1. Crear namespace
# ============================================
echo_message "$BLUE" "Configurando namespace..."

kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
echo_message "$GREEN" "Namespace creado: $NAMESPACE"
echo ""

# ============================================
# 2. Desplegar infraestructura
# ============================================
echo_message "$BLUE" "Desplegando infraestructura (PostgreSQL + Kafka)..."

kubectl apply -f k8s/infrastructure/

echo_message "$YELLOW" "Esperando a que PostgreSQL esté listo..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=180s -n $NAMESPACE

echo_message "$YELLOW" "Esperando a que Kafka esté listo..."
kubectl wait --for=condition=ready pod -l app=kafka --timeout=240s -n $NAMESPACE

echo_message "$GREEN" "Infraestructura desplegada"
echo ""

# ============================================
# 3. Desplegar aplicación desde GHCR
# ============================================
echo_message "$BLUE" "Desplegando aplicación desde GHCR..."

# Obtener última imagen de GHCR
LATEST_IMAGE=$(docker pull ghcr.io/zetta1973/ecommerce-api/ecommerce-api:latest 2>/dev/null && echo "ghcr.io/zetta1973/ecommerce-api/ecommerce-api:latest" || echo "ghcr.io/zetta1973/ecommerce-api/ecommerce-api:main")

echo "Usando imagen: $LATEST_IMAGE"

# Reemplazar imagen en deployment
sed "s|ghcr.io/zetta1973/ecommerce-api.*|$LATEST_IMAGE|g" k8s/application/deployment.yaml | kubectl apply -f -

echo_message "$YELLOW" "Esperando a que la aplicación esté lista..."
kubectl wait --for=condition=ready pod -l app=ecommerce-api --timeout=180s -n $NAMESPACE

echo_message "$GREEN" "Aplicación desplegada"
echo ""

# ============================================
# 4. Configurar port-forwarding
# ============================================
echo_message "$BLUE" "Configurando port-forwarding..."

# Obtener nombre del pod
POD_NAME=$(kubectl get pods -l app=ecommerce-api -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}')

# Iniciar port-forwarding en segundo plano
kubectl port-forward pod/$POD_NAME 8080:8080 -n $NAMESPACE > /dev/null 2>&1 &
PORT_FORWARD_PID=$!

echo_message "$GREEN" "Port-forwarding configurado (puedo: 8080)"
echo "  - Para detener: kill $PORT_FORWARD_PID"
echo ""

# ============================================
# 5. Verificar despliegue
# ============================================
echo_message "$BLUE" "Verificando despliegue..."

# Esperar un momento
sleep 5

# Probar health endpoint
if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo_message "$GREEN" "✓ Health check: PASSED"
else
    echo_message "$RED" "✗ Health check: FAILED"
    echo "Intentando obtener logs..."
    kubectl logs -l app=ecommerce-api -n $NAMESPACE --tail=50
    exit 1
fi

echo ""
echo_message "$GREEN" "Despliegue completado con éxito!"
echo ""
echo "📋 Información:"
echo "  - URL: http://localhost:8080"
echo "  - Namespace: $NAMESPACE"
echo "  - Imagen: $LATEST_IMAGE"
echo ""
echo "🔧 Comandos útiles:"
echo "  - Ver pods: kubectl get pods -n $NAMESPACE"
echo "  - Ver logs: kubectl logs -l app=ecommerce-api -n $NAMESPACE -f"
echo "  - Ver servicios: kubectl get svc -n $NAMESPACE"
echo "  - Parar port-forwarding: kill $PORT_FORWARD_PID"
echo "  - Eliminar todo: kubectl delete -f k8s/ -n $NAMESPACE"
