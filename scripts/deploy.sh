#!/bin/bash

# Deploy Script for Ecommerce API
# Usage: ./scripts/deploy.sh [environment] [version]

set -e

ENVIRONMENT=${1:-"staging"}
VERSION=${2:-"latest"}
IMAGE_TAG="ghcr.io/zetta1973/ecommerce-api:$VERSION"

echo "🚀 Deploying Ecommerce API to environment: $ENVIRONMENT"
echo "📦 Version: $VERSION"
echo "📦 Image: $IMAGE_TAG"

case $ENVIRONMENT in
  "staging")
    echo "🔧 Deploying to staging environment..."
    kubectl apply -f k8s/overlays/staging/
    echo "✅ Deployed to staging successfully!"
    ;;
  "production")
    echo "🏭 Deploying to production environment..."
    kubectl apply -f k8s/overlays/production/
    echo "✅ Deployed to production successfully!"
    ;;
  "local")
    echo "🏠 Deploying to local Kubernetes..."
    kubectl apply -f k8s/deployment.yaml
    echo "✅ Deployed to local successfully!"
    ;;
  *)
    echo "❌ Invalid environment. Use: staging, production, or local"
    exit 1
    ;;
esac

echo "🔍 Checking deployment status..."
kubectl rollout status deployment/ecommerce-api -n default

echo "🌐 Getting service URL..."
SERVICE_URL=$(kubectl get service ecommerce-api-service -n default -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
if [ -n "$SERVICE_URL" ]; then
    echo "📍 Service URL: http://$SERVICE_URL"
else
    echo "⚠️ Service not externally accessible"
fi

echo "✅ Deployment completed!"