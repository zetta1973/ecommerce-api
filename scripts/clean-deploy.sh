#!/bin/bash

# ========================================================
# SCRIPT: clean-deploy.sh
# DESCRIPCIÓN: Limpia deployments antiguos y despliega nueva imagen
# USO:
#   ./clean-deploy.sh
#   ./clean-deploy.sh 1.2.0
#   ./clean-deploy.sh ci-latest
# ========================================================

set -e

NAMESPACE="ecommerce"
IMAGE_REPO="ghcr.io/zetta1973/ecommerce-api"
APP_NAME="ecommerce-api"

# Determinar la imagen a usar
if [ $# -eq 1 ]; then
    TAG="$1"
    echo "🎯 Modo específico: usando tag $TAG"
else
    TAG="ci-latest"
    echo "🧹 Modo limpio: usando tag 'ci-latest'"
fi

IMAGE_NAME="$IMAGE_REPO:$TAG"

echo "📦 Imagen: $IMAGE_NAME"
echo "🧹 Iniciando limpieza y despliegue limpio..."

# 1. Verificaciones básicas
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado."
    exit 1
fi

kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# 2. Limpiar deployments antiguos
echo "🗑️  Limpiando deployments anteriores..."
kubectl delete deployment "$APP_NAME" -n "$NAMESPACE" --ignore-not-found=true

# 3. Limpiar pods colgados/terminados
echo "🧹 Limpiando pods terminados..."
kubectl delete pods -l app="$APP_NAME" -n "$NAMESPACE" --field-selector status.phase!=Running --ignore-not-found=true

# 4. Limpiar ReplicaSets antiguos
echo "🧹 Limpiando ReplicaSets antiguos..."
kubectl get replicaset -l app="$APP_NAME" -n "$NAMESPACE" -o name | xargs -r kubectl delete -n "$NAMESPACE" --ignore-not-found=true

# 5. Esperar a que termine la limpieza
echo "⏳ Esperando a que termine la limpieza..."
sleep 5

# 6. Descargar imagen si es necesario
if command -v docker &> /dev/null; then
    echo "📥 Descargando imagen: $IMAGE_NAME"
    docker pull "$IMAGE_NAME"
fi

# 7. Crear namespace si no existe
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null

# 8. Verificar manifiesto
MANIFEST="k8s/deployment.yaml"
if [ ! -f "$MANIFEST" ]; then
    echo "❌ Error: No se encuentra el manifiesto $MANIFEST"
    exit 1
fi

# 9. Actualizar imagen en manifiesto
echo "🔄 Actualizando imagen en el manifiesto..."
sed -i.bak "s|image:.*|image: $IMAGE_NAME|" "$MANIFEST"

# 10. Despliegue limpio
echo "🚀 Desplegando nueva versión..."
kubectl apply -f "$MANIFEST"

# 11. Esperar rollout
echo "⏳ Esperando a que los pods estén listos..."
kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=180s

# 12. Estado final
echo ""
echo "✅ ¡Limpieza y despliegue completados!"
echo "📊 Estado final:"
kubectl get pods -n "$NAMESPACE" -l app="$APP_NAME"
echo ""
echo "🔗 Para acceder a la API:"
echo "   kubectl port-forward svc/ecommerce-api-service 8080:80 -n $NAMESPACE"
echo "   Luego abre: http://localhost:8080"
echo ""
echo "📋 Resumen de recursos:"
echo "   Pods: $(kubectl get pods -n $NAMESPACE -l app=$APP_NAME --no-headers | wc -l)"
echo "   Services: $(kubectl get svc -n $NAMESPACE -l app=$APP_NAME --no-headers | wc -l)"
echo "   ReplicaSets: $(kubectl get replicaset -n $NAMESPACE -l app=$APP_NAME --no-headers | wc -l)"