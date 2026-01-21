#!/bin/bash

set -e

usage() {
    echo "Usage: $0 [namespace]"
    echo ""
    echo "Deploy latest image from GHCR to Rancher Desktop"
    echo ""
    echo "Arguments:"
    echo "  namespace    Kubernetes namespace (default: ecommerce)"
    echo ""
    echo "Examples:"
    echo "  $0 ecommerce"
    exit 1
}

NAMESPACE="${1:-ecommerce}"
REPO="zetta1973/ecommerce-api"
GHCR_IMAGE="ghcr.io/$REPO/ecommerce-api"

echo "=========================================="
echo "  Deploy to $NAMESPACE"
echo "=========================================="
echo ""

echo "[1/3] Finding latest image from GitHub..."
LATEST_SHA=$(python3 -c "
import urllib.request
import json
url = 'https://api.github.com/repos/$REPO/actions/workflows/ci-cd-kind.yml/runs?per_page=1'
req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    print(data['workflow_runs'][0]['head_sha'] if data['workflow_runs'] else '')
" 2>/dev/null)

if [ -z "$LATEST_SHA" ]; then
    echo "Error: No workflow runs found"
    exit 1
fi

LATEST_IMAGE="${GHCR_IMAGE}:main-${LATEST_SHA}"
echo "Found: $LATEST_IMAGE"
echo ""

echo "[2/3] Pulling image..."
if ! docker pull "$LATEST_IMAGE" 2>/dev/null; then
    echo "SHA-specific image not found, falling back to 'main' tag..."
    LATEST_IMAGE="${GHCR_IMAGE}:main"
    docker pull "$LATEST_IMAGE"
fi
echo ""

echo "[3/3] Deploying to Kubernetes..."
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# Deploy infrastructure
kubectl apply -f k8s/infrastructure/

# Deploy application with specific image
kubectl kustomize "k8s/application" | \
    sed "s|ghcr.io/zetta1973/ecommerce-api.*|$LATEST_IMAGE|g" | \
    kubectl apply -f -

echo ""
echo "Waiting for rollout..."
kubectl rollout status deployment/ecommerce-api -n "$NAMESPACE" --timeout=180s

echo ""
echo "=========================================="
echo "  Deployed Successfully!"
echo "=========================================="
echo "Image: $LATEST_IMAGE"
echo "Namespace: $NAMESPACE"
echo ""
echo "Commands:"
echo "  kubectl port-forward -n $NAMESPACE svc/ecommerce-api 8080:8080"
echo "  kubectl logs -n $NAMESPACE -l app=ecommerce-api -f"
