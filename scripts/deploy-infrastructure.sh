#!/bin/bash

# ====================================================================================
# SCRIPT: deploy-infrastructure.sh
# DESCRIPCIÓN: Despliega toda la infraestructura (postgres, kafka, jenkins, kafka-ui)
# USO: ./deploy-infrastructure.sh
# ====================================================================================

set -e

NAMESPACE="ecommerce"
INFRA_DIR="k8s/infrastructure"

echo "🏗️  Desplegando infraestructura en namespace '$NAMESPACE'..."

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Error: 'kubectl' no encontrado. ¿Tienes Rancher Desktop instalado?"
    exit 1
fi

# Verificar conexión al clúster
kubectl cluster-info >/dev/null 2>&1 || { echo "❌ No estás conectado a un clúster de Kubernetes."; exit 1; }

# Crear namespace si no existe
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null

echo "🔐 Aplicando secrets..."
kubectl apply -f "$INFRA_DIR/ecommerce-secret.yaml"

echo "🐘 Desplegando PostgreSQL..."
kubectl apply -f "$INFRA_DIR/postgres.yaml"

echo "📨 Desplegando Kafka..."
kubectl apply -f "$INFRA_DIR/kafka.yaml"

echo "🔧 Desplegando Jenkins..."
kubectl apply -f "$INFRA_DIR/jenkins.yaml"

echo "🎛️  Desplegando Kafka UI..."
kubectl apply -f "$INFRA_DIR/kafka-ui.yaml"

echo "⏳ Esperando a que los pods estén listos..."
kubectl wait --for=condition=ready pod -l app=postgres -n "$NAMESPACE" --timeout=120s
kubectl wait --for=condition=ready pod -l app=kafka -n "$NAMESPACE" --timeout=120s
kubectl wait --for=condition=ready pod -l app=jenkins -n "$NAMESPACE" --timeout=120s
kubectl wait --for=condition=ready pod -l app=kafka-ui -n "$NAMESPACE" --timeout=120s

echo ""
echo "✅ ¡Infraestructura desplegada con éxito!"
echo "📊 Estado de los pods:"
kubectl get pods -n "$NAMESPACE"
echo ""
echo "🔗 Servicios disponibles:"
echo "  PostgreSQL: postgres:5432 (interno)"
echo "  Kafka: kafka:9092 (interno)"
echo "  Jenkins: http://localhost:31832"
echo "  Kafka UI: http://localhost:30629"
echo ""
echo "💡 Para acceder a Jenkins y Kafka UI:"
echo "   kubectl port-forward svc/jenkins 8080:8080 -n $NAMESPACE"
echo "   kubectl port-forward svc/kafka-ui 8080:8080 -n $NAMESPACE"
