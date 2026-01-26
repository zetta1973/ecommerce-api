#!/bin/bash

# ========================================================
# SCRIPT: pull-and-deploy.sh
# DESCRIPCIÓN: Descarga imagen de GHCR y la despliega en Kubernetes local
# USO:
#   ./pull-and-deploy.sh
#   ./pull-and-deploy.sh 1.2.0
#   ./pull-and-deploy.sh ci-latest
# ========================================================

set -e

NAMESPACE="ecommerce"
IMAGE_REPO="ghcr.io/zetta1973/ecommerce-api"

# Determinar la imagen a usar
if [ $# -eq 1 ]; then
    TAG="$1"
    echo "🎯 Modo específico: usando tag $TAG"
else
    TAG="ci-latest"
    echo "⚡ Modo CI: usando tag 'ci-latest' (imagen de GitHub Actions)"
fi

IMAGE_NAME="$IMAGE_REPO:$TAG"

echo "📦 Imagen: $IMAGE_NAME"
echo "🔄 Iniciando proceso de pull y despliegue..."

# 1. Verificar Docker está disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Error: 'docker' no encontrado. ¿Tienes Docker instalado?"
    exit 1
fi

# 2. Verificar kubectl está disponible
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado. ¿Tienes Rancher Desktop instalado?"
    exit 1
fi

# 3. Verificar conexión al clúster
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# 4. Descargar imagen del registry
echo "📥 Descargando imagen de GitHub Container Registry..."
docker pull "$IMAGE_NAME"

# 5. Tag local para el cluster (si es necesario)
if command -v kind &> /dev/null; then
    echo "🏷️  Detectado Kind cluster, cargando imagen..."
    kind load docker-image "$IMAGE_NAME" --name rancher-desktop 2>/dev/null || echo "⚠️  No se pudo cargar en Kind (puede que no sea Kind)"
elif command -v rancher-desktop &> /dev/null; then
    echo "🐄 Rancher Desktop detectado, imagen disponible automáticamente"
else
    echo "💡 Asegúrate que tu cluster local pueda acceder a la imagen Docker local"
fi

# 6. Crear namespace si no existe
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null

# 7. Verificar manifiesto existe
MANIFEST="k8s/deployment.yaml"
if [ ! -f "$MANIFEST" ]; then
    echo "❌ Error: No se encuentra el manifiesto $MANIFEST"
    exit 1
fi

# 8. Actualizar la imagen en el manifiesto
echo "🔄 Actualizando imagen en el manifiesto..."
sed -i.bak "s|image:.*|image: $IMAGE_NAME|" "$MANIFEST"

# 9. Aplicar manifiesto
echo "🔧 Aplicando manifiestos en Kubernetes..."
kubectl apply -f "$MANIFEST"

# 10. Esperar rollout
echo "⏳ Esperando a que los pods estén listos..."
kubectl rollout status deployment/ecommerce-api -n "$NAMESPACE" --timeout=180s

# 11. Mostrar resultado
echo ""
echo "✅ ¡Pull y despliegue completados!"
echo "📊 Estado final:"
kubectl get pods -n "$NAMESPACE" -l app=ecommerce-api
echo ""
echo "🔗 Para acceder a la API:"
echo "   kubectl port-forward svc/ecommerce-api-service 8080:80 -n $NAMESPACE"
echo "   Luego abre: http://localhost:8080"
echo ""
echo "📋 Comandos útiles:"
echo "   Ver logs: kubectl logs -f deployment/ecommerce-api -n $NAMESPACE"
echo "   Ver servicios: kubectl get svc -n $NAMESPACE"