#!/bin/bash

# Build Script for Ecommerce API
# Usage: ./scripts/build.sh [environment]

set -e

ENVIRONMENT=${1:-"local"}
VERSION=${2:-"latest"}

echo "🏗 Building Ecommerce API for environment: $ENVIRONMENT"
echo "📦 Version: $VERSION"

# Clean and compile
echo "🧹 Cleaning and compiling..."
mvn clean compile

# Run tests
echo "🧪 Running tests..."
mvn test

# Package application
echo "📦 Packaging application..."
mvn package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t ecommerce-api:$VERSION .

echo "✅ Build completed successfully!"
echo "📦 Docker image: ecommerce-api:$VERSION"
echo "📁 JAR location: target/ecommerce-api-$VERSION.jar"