#!/bin/bash

# ========================================================
# SCRIPT HÍBRIDO: deploy-local.sh
# DESCRIPCIÓN: Despliega en Kubernetes local (Rancher Desktop)
# - Sin argumentos: usa 'ci-latest'
# - Con argumento: usa el SHA proporcionado
# USO:
#   ./deploy-local.sh
#   ./deploy-local.sh a1b2c3d4e5f6
# ========================================================

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

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado. ¿Tienes Rancher Desktop instalado y configurado?"
    exit 1
fi

# Verificar conexión al clúster
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# Crear namespace si no existe
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null

# Ruta del manifiesto
MANIFEST_DIR="k8s"
MANIFEST="$MANIFEST_DIR/deployment.yaml"
mkdir -p "$MANIFEST_DIR"

# Generar manifiesto si no existe
if [ ! -f "$MANIFEST" ]; then
    echo "📝 Generando manifiesto básico en $MANIFEST..."
    cat > "$MANIFEST" << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ecommerce-api
  namespace: $NAMESPACE
  labels:
    app: ecommerce-api
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ecommerce-api
  template:
    metadata:
      labels:
        app: ecommerce-api
    spec:
      containers:
      - name: ecommerce-api
        image: $IMAGE_NAME
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "local"
---
apiVersion: v1
kind: Service
metadata:
  name: ecommerce-api
  namespace: $NAMESPACE
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: ecommerce-api
EOF
else
    # Si ya existe, reemplazar la imagen
    sed -i.bak "s|image:.*|image: $IMAGE_NAME|" "$MANIFEST"
    echo "🔄 Actualizada la imagen en el manifiesto existente."
fi

# Aplicar
echo "🔧 Aplicando manifiestos..."
kubectl apply -f "$MANIFEST"

# Esperar rollout
echo "⏳ Esperando a que el pod esté listo..."
kubectl rollout status deployment/ecommerce-api -n "$NAMESPACE" --timeout=120s

# Resultado
echo ""
echo "✅ ¡Despliegue completado!"
echo "📊 Pods:"
kubectl get pods -n "$NAMESPACE" -l app=ecommerce-api
echo ""
echo "🔗 Para acceder:"
echo "   kubectl port-forward svc/ecommerce-api 8080:80 -n $NAMESPACE"
echo "   Luego abre: http://localhost:8080"
