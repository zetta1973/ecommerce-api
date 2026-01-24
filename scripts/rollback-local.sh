#!/bin/bash

# ========================================================
# SCRIPT: rollback-local.sh
# DESCRIPCIÓN: Revierte el deployment ecommerce-api a la versión anterior
# REQUISITO: El deployment debe haber tenido al menos 2 revisiones
# USO: ./rollback-local.sh
# ========================================================

set -e

NAMESPACE="ecommerce"
DEPLOYMENT="ecommerce-api"

echo "🔙 Iniciando rollback del deployment '$DEPLOYMENT'..."

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado."
    exit 1
fi

# Verificar conexión
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster."; exit 1; }

# Verificar si el deployment existe
if ! kubectl get deployment "$DEPLOYMENT" -n "$NAMESPACE" >/dev/null 2>&1; then
    echo "❌ El deployment '$DEPLOYMENT' no existe en el namespace '$NAMESPACE'."
    exit 1
fi

# Mostrar historial de revisiones
echo "📋 Historial de revisiones:"
kubectl rollout history deployment/"$DEPLOYMENT" -n "$NAMESPACE"

echo ""
read -p "¿Deseas revertir a la revisión anterior? (s/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "⏹️  Rollback cancelado."
    exit 0
fi

# Ejecutar rollback
echo "🔄 Ejecutando rollback..."
kubectl rollout undo deployment/"$DEPLOYMENT" -n "$NAMESPACE"

# Esperar a que termine
echo "⏳ Esperando a que el rollback se complete..."
kubectl rollout status deployment/"$DEPLOYMENT" -n "$NAMESPACE" --timeout=120s

echo "✅ ¡Rollback completado!"
echo "📊 Nueva imagen desplegada:"
kubectl get deployment "$DEPLOYMENT" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}'
