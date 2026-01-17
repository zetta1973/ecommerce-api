#!/bin/bash

# Crear cluster Kind (solo si no existe)
if ! kind get clusters | grep -q ecommerce; then
    echo "Creating Kind cluster..."
    kind create cluster --name ecommerce
fi

# Desplegar infraestructura (PostgreSQL + Kafka)
echo "Deploying infrastructure..."
kubectl apply -f k8s/infrastructure/

# Esperar a que estén listos
echo "Waiting for PostgreSQL and Kafka to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
kubectl wait --for=condition=ready pod -l app=kafka --timeout=180s

echo "Infrastructure is ready! 🚀"
