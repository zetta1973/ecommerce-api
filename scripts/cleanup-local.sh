#!/bin/bash

# ========================================================
# SCRIPT: cleanup-local.sh
# DESCRIPCIÓN: Elimina todos los recursos de ecommerce-api del namespace 'ecommerce'
# USO: ./cleanup-local.sh
# ========================================================

set -e

NAMESPACE="ecommerce"
APP_NAME="ecommerce-api"

echo "🧹 Iniciando limpieza de recursos en namespace '$NAMESPACE'..."

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado. ¿Tienes Rancher Desktop instalado?"
    exit 1
fi

# Verificar conexión al clúster
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# Verificar si el namespace existe
if kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
    echo "🗑️  Eliminando deployments y servicios..."
    kubectl delete deployment,service --all -n "$NAMESPACE" --ignore-not-found=true

    echo "⏳ Esperando a que los pods terminen..."
    kubectl wait --for=delete pod --all -n "$NAMESPACE" --timeout=60s 2>/dev/null || true

    echo "📦 Eliminando secrets y configmaps..."
    kubectl delete secret,configmap --all -n "$NAMESPACE" --ignore-not-found=true

    echo "🗑️  Eliminando el namespace completo..."
    kubectl delete namespace "$NAMESPACE" --ignore-not-found=true

    echo "✅ Limpieza completada. Namespace '$NAMESPACE' eliminado."
else
    echo "ℹ️  El namespace '$NAMESPACE' no existe. Nada que limpiar."
fi

echo ""
echo "✨ ¡Listo! Tu entorno local está limpio."
