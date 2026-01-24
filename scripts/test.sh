#!/bin/bash

# Test Script for Ecommerce API
# Usage: ./scripts/test.sh [type]

set -e

TEST_TYPE=${1:-"unit"}

echo "🧪 Running tests: $TEST_TYPE"

case $TEST_TYPE in
  "unit")
    echo "🔬 Running unit tests..."
    mvn test
    ;;
  "integration")
    echo "🔗 Running integration tests..."
    mvn verify
    ;;
  "coverage")
    echo "📊 Running tests with coverage..."
    mvn clean verify jacoco:report
    echo "📈 Coverage report generated: target/site/jacoco/index.html"
    ;;
  "all")
    echo "🔬 Running all tests..."
    mvn clean verify
    ;;
  *)
    echo "❌ Invalid test type. Use: unit, integration, coverage, or all"
    exit 1
    ;;
esac

echo "✅ Tests completed!"