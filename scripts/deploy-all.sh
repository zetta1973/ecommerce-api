#!/bin/bash

# ==================================================================
# SCRIPT: deploy-all.sh
# DESCRIPCIÓN: Despliega toda la infraestructura y la aplicación
# USO: ./deploy-all.sh [tag]
# ==================================================================

set -e

NAMESPACE="ecommerce"
IMAGE_REPO="ghcr.io/zetta1973/ecommerce-api"

# Determinar la imagen a usar
if [ $# -eq 1 ]; then
    TAG="$1"
    echo "🎯 Modo específico: desplegando commit $TAG"
else
    TAG="ci-latest"
    echo "⚡ Modo rápido: desplegando etiqueta 'ci-latest'"
fi

IMAGE_NAME="$IMAGE_REPO:$TAG"
echo "📦 Imagen: $IMAGE_NAME"

echo "🏗️  Iniciando despliegue completo..."

# 1. Desplegar infraestructura
echo "📋 Paso 1: Desplegando infraestructura..."
./scripts/deploy-infrastructure.sh

# 2. Actualizar imagen en deployment.yaml
echo "📋 Paso 2: Actualizando imagen de la aplicación..."
sed -i.bak "s|image:.*|image: $IMAGE_NAME|" "k8s/deployment.yaml"

# 3. Desplegar aplicación
echo "📋 Paso 3: Desplegando aplicación..."
kubectl apply -f "k8s/deployment.yaml"

# 4. Esperar rollout
echo "⏳ Esperando a que los pods estén listos..."
kubectl wait --for=condition=ready pod -l app=ecommerce-api -n "$NAMESPACE" --timeout=180s

echo ""
echo "✅ ¡Despliegue completo terminado con éxito!"
echo "📊 Estado final:"
kubectl get pods -n "$NAMESPACE"
echo ""
echo "🔗 Servicios disponibles:"
echo "  API: kubectl port-forward svc/ecommerce-api-service 8080:80 -n $NAMESPACE"
echo "  Jenkins: kubectl port-forward svc/jenkins 8080:8080 -n $NAMESPACE"  
echo "  Kafka UI: kubectl port-forward svc/kafka-ui 8080:8080 -n $NAMESPACE"
echo ""
echo "🌐 Accesos locales:"
echo "  API: http://localhost:8080"
echo "  Jenkins: http://localhost:8080 (después del port-forward de Jenkins)"
echo "  Kafka UI: http://localhost:8080 (después del port-forward de Kafka UI)"
