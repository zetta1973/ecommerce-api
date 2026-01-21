#!/bin/bash

# ============================================
# Ecommerce API - Despliegue en Rancher Local
# ============================================
# Uso: bash scripts/deploy-to-rancher.sh
# 
# Este script:
# 1. Te da opción de usar imagen del artefacto o descargar desde GHCR
# 2. Actualiza el deployment con la imagen correcta
# 3. Aplica los manifests a tu clúster de Rancher
# 4. Verifica que los pods estén listos
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

# Función para verificar si un comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verificar prerequisitos
echo_message "$YELLOW" "Verificando prerequisitos..."

if ! command_exists kubectl; then
    echo_message "$RED" "kubectl no está instalado"
    exit 1
fi

if ! command_exists docker; then
    echo_message "$RED" "Docker no está instalado"
    exit 1
fi

echo_message "$GREEN" "Todos los prerequisitos están instalados"
echo ""

# ============================================
# 1. Seleccionar fuente de la imagen
# ============================================
echo_message "$BLUE" "Selecciona la fuente de la imagen Docker:"
echo ""
echo "Opción 1: Usar imagen del artefacto de GitHub Actions"
echo "Opción 2: Descargar imagen directamente desde GHCR"
echo ""

read -p "Selecciona una opción [1-2]: " option

case $option in
    1)
        echo_message "$YELLOW" "Usando imagen del artefacto..."
        
        # Verificar si el artefacto existe
        if [ ! -f "ecommerce-api.tar" ]; then
            echo_message "$RED" "El archivo ecommerce-api.tar no fue encontrado"
            echo "Descarga el artefacto desde GitHub Actions antes de ejecutar este script"
            exit 1
        fi
        
        # Cargar imagen desde artefacto
        echo_message "$YELLOW" "Cargando imagen desde artefacto..."
        docker load -i ecommerce-api.tar
        IMAGE_NAME="ecommerce-api:ci"
        echo_message "$GREEN" "Imagen cargada: $IMAGE_NAME"
        ;;
    
    2)
        echo_message "$YELLOW" "Descargando imagen desde GHCR..."
        
        # Solicitar token de GitHub
        read -p "Ingresa tu token de GitHub: " github_token
        read -p "Ingresa tu usuario de GitHub: " github_user
        
        # Autenticarse en GHCR
        echo "$github_token" | docker login ghcr.io -u "$github_user" --password-stdin
        
        # Obtener el último commit SHA (o permitir ingresarlo)
        echo ""
        echo "Opciones para seleccionar imagen:"
        echo "1. Usar la última imagen pushada (recomendado)"
        echo "2. Ingresar un commit SHA específico"
        read -p "Selecciona una opción [1-2]: " img_option
        
        if [ "$img_option" = "1" ]; then
            # Obtener el último commit SHA
            latest_commit=$(git rev-parse HEAD)
            IMAGE_TAG="ghcr.io/zetta1973/ecommerce-api:ci-$latest_commit"
        else
            read -p "Ingresa el commit SHA: " custom_commit
            IMAGE_TAG="ghcr.io/zetta1973/ecommerce-api:ci-$custom_commit"
        fi
        
        # Descargar imagen
        echo_message "$YELLOW" "Descargando imagen: $IMAGE_TAG"
        docker pull "$IMAGE_TAG"
        IMAGE_NAME="$IMAGE_TAG"
        echo_message "$GREEN" "Imagen descargada: $IMAGE_NAME"
        ;;
    
    *)
        echo_message "$RED" "Opción no válida"
        exit 1
        ;;
esac

echo ""

# ============================================
# 2. Actualizar el deployment con la imagen
# ============================================
echo_message "$BLUE" "Actualizando deployment con la imagen: $IMAGE_NAME"

# Reemplazar la imagen en el deployment
sed -i "s|ghcr.io/zetta1973/ecommerce-api.*|$IMAGE_NAME|g" k8s/application/deployment.yaml
echo_message "$GREEN" "Deployment actualizado"
echo ""

# ============================================
# 3. Aplicar manifests a Kubernetes
# ============================================
echo_message "$BLUE" "Aplicando manifests a Kubernetes..."

# Aplicar secrets
kubectl apply -f k8s/application/secrets.yaml -n $NAMESPACE
echo_message "$GREEN" "Secrets aplicados"

# Aplicar configmap
kubectl apply -f k8s/application/configmap.yaml -n $NAMESPACE
echo_message "$GREEN" "ConfigMap aplicado"

# Aplicar deployment y service
kubectl apply -f k8s/application/ -n $NAMESPACE
echo_message "$GREEN" "Aplicación desplegada"
echo ""

# ============================================
# 4. Verificar que los pods estén listos
# ============================================
echo_message "$BLUE" "Esperando a que la aplicación esté lista..."

# Esperar a que el pod esté listo
if ! kubectl wait --for=condition=ready pod -l app=ecommerce-api --timeout=180s -n $NAMESPACE; then
    echo_message "$RED" "❌ El pod no se pudo iniciar"
    echo "Obteniendo información de diagnóstico..."
    kubectl get pods -l app=ecommerce-api -n $NAMESPACE -o wide
    kubectl describe pod -l app=ecommerce-api -n $NAMESPACE
    kubectl logs -l app=ecommerce-api -n $NAMESPACE --tail=50 || echo "No hay logs disponibles"
    exit 1
fi

echo_message "$GREEN" "✅ La aplicación está lista!"
echo ""

# ============================================
# 5. Mostrar información del despliegue
# ============================================
echo "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo "${GREEN}✓ Despliegue completado con éxito!${NC}"
echo "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo "📋 Información del despliegue:"
echo "  - Namespace: $NAMESPACE"
echo "  - Imagen: $IMAGE_NAME"
echo "  - URL: http://localhost:8080 (necesita port-forwarding)"
echo ""
echo "🔧 Comandos útiles:"
echo "  - Ver pods: kubectl get pods -n $NAMESPACE"
echo "  - Ver logs: kubectl logs -l app=ecommerce-api -n $NAMESPACE -f"
echo "  - Ver servicios: kubectl get svc -n $NAMESPACE"
echo "  - Port-forwarding: kubectl port-forward svc/ecommerce-api 8080:8080 -n $NAMESPACE"
echo "  - Eliminar despliegue: kubectl delete -f k8s/application/ -n $NAMESPACE"
echo ""
echo "🚀 ¡La aplicación está lista para usar!"
