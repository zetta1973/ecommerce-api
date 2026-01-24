#!/bin/bash

# ========================================================
# SCRIPT: logs-local.sh
# DESCRIPCIÓN: Muestra los logs en tiempo real de la aplicación ecommerce-api
# USO: ./logs-local.sh          → sigue los logs
#      ./logs-local.sh --last   → muestra los últimos logs (sin seguir)
# ========================================================

set -e

NAMESPACE="ecommerce"
APP_LABEL="app=ecommerce-api"

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado. ¿Tienes Rancher Desktop instalado?"
    exit 1
fi

# Verificar conexión al clúster
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# Verificar si hay pods
if ! kubectl get pods -n "$NAMESPACE" -l "$APP_LABEL" --no-headers | grep -q .; then
    echo "ℹ️  No se encontraron pods con la etiqueta '$APP_LABEL' en el namespace '$NAMESPACE'."
    echo "   ¿Está desplegada la aplicación?"
    exit 1
fi

FOLLOW=true
if [[ "$1" == "--last" ]]; then
    FOLLOW=false
    echo "📜 Mostrando últimos logs (sin seguir)..."
else
    echo "👀 Siguiendo logs en tiempo real (Ctrl+C para salir)..."
fi

if [ "$FOLLOW" = true ]; then
    kubectl logs -n "$NAMESPACE" -l "$APP_LABEL" -c ecommerce-api -f --tail=50
else
    kubectl logs -n "$NAMESPACE" -l "$APP_LABEL" -c ecommerce-api --tail=200
fi
