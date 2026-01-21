#!/bin/bash

# ============================================
# Ecommerce API - Colección de comandos curl
# ============================================
# Servidor: http://localhost:8080
# Profile: ci (H2 en memoria)
# ============================================

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "  Ecommerce API - Pruebas con curl"
echo "=========================================="
echo ""

# Colores para输出的
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================
# AUTENTICACIÓN
# ============================================
echo -e "${BLUE}[AUTH] Registro de usuario${NC}"
echo "curl -X POST $BASE_URL/auth/register"
echo '  -H "Content-Type: application/json"'
echo '  -d '\''{"username":"testuser","email":"test@test.com","password":"test123"}'\'''
echo ""
echo -e "${YELLOW}# Copia y pega para ejecutar:${NC}"
curl -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@test.com","password":"test123"}'
echo ""
echo ""

echo -e "${BLUE}[AUTH] Login usuario${NC}"
echo "curl -X POST $BASE_URL/auth/login"
echo '  -H "Content-Type: application/json"'
echo '  -d '\''{"email":"test@test.com","password":"test123"}'\'''
echo ""
echo -e "${YELLOW}# Copia y pega para ejecutar (guarda el token):${NC}"
TOKEN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}')
echo "$TOKEN_RESPONSE"
TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo ""
echo "Token: ${TOKEN:0:50}..."
echo ""

echo -e "${BLUE}[AUTH] Refresh Token${NC}"
echo "curl -X POST $BASE_URL/auth/refresh"
echo '  -H "Content-Type: application/json"'
echo '  -d '\''{"refreshToken":"<TU_REFRESH_TOKEN>"}'\'''
echo ""

# ============================================
# ADMIN
# ============================================
echo -e "${BLUE}[ADMIN] Ping público${NC}"
echo "curl $BASE_URL/admin/ping"
echo ""
curl $BASE_URL/admin/ping
echo ""
echo ""

echo -e "${BLUE}[ADMIN] Listar usuarios (requiere token)${NC}"
echo "curl $BASE_URL/admin/users"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$TOKEN" ]; then
  curl $BASE_URL/admin/users \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[ADMIN] Asignar permiso a rol${NC}"
echo "curl -X POST $BASE_URL/admin/roles/assign"
echo '  -H "Content-Type: application/json"'
echo '  -H "Authorization: Bearer $TOKEN"'
echo '  -d '\''{"roleName":"ROLE_USER","permissionName":"READ_PRODUCTS"}'\'''
echo ""

# ============================================
# PRODUCTOS
# ============================================
echo -e "${BLUE}[PRODUCTS] Listar todos (público)${NC}"
echo "curl $BASE_URL/api/products"
echo ""
curl $BASE_URL/api/products
echo ""
echo ""

echo -e "${BLUE}[PRODUCTS] Buscar productos${NC}"
echo "curl \"$BASE_URL/api/products/search?name=product\""
echo ""
curl "$BASE_URL/api/products/search?name=product"
echo ""
echo ""

echo -e "${BLUE}[PRODUCTS] Crear producto (requiere token)${NC}"
echo "curl -X POST $BASE_URL/api/products"
echo '  -H "Content-Type: application/json"'
echo '  -H "Authorization: Bearer \$TOKEN"'
echo '  -d '\''{"name":"Producto Test","description":"Descripción","price":29.99,"stock":100}'\'''
echo ""
if [ ! -z "$TOKEN" ]; then
  PRODUCT_RESPONSE=$(curl -s -X POST $BASE_URL/api/products \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"Producto Test","description":"Descripción del producto","price":29.99,"stock":100}')
  echo "$PRODUCT_RESPONSE"
  PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
  echo "Product ID: $PRODUCT_ID"
fi
echo ""

echo -e "${BLUE}[PRODUCTS] Ver producto específico${NC}"
echo "curl $BASE_URL/api/products/\$PRODUCT_ID"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$PRODUCT_ID" ]; then
  curl $BASE_URL/api/products/$PRODUCT_ID \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[PRODUCTS] Ver stock producto${NC}"
echo "curl $BASE_URL/api/products/\$PRODUCT_ID/stock"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$PRODUCT_ID" ]; then
  curl $BASE_URL/api/products/$PRODUCT_ID/stock \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[PRODUCTS] Actualizar producto${NC}"
echo "curl -X PUT $BASE_URL/api/products/\$PRODUCT_ID"
echo '  -H "Content-Type: application/json"'
echo '  -H "Authorization: Bearer \$TOKEN"'
echo '  -d '\''{"name":"Producto Actualizado","description":"Nueva descripción","price":39.99,"stock":150}'\'''
echo ""
if [ ! -z "$PRODUCT_ID" ]; then
  curl -X PUT $BASE_URL/api/products/$PRODUCT_ID \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"Producto Actualizado","description":"Nueva descripción","price":39.99,"stock":150}'
  echo ""
fi
echo ""

# ============================================
# ÓRDENES
# ============================================
echo -e "${BLUE}[ORDERS] Crear orden (requiere token)${NC}"
echo "curl -X POST $BASE_URL/api/orders"
echo '  -H "Content-Type: application/json"'
echo '  -H "Authorization: Bearer \$TOKEN"'
echo '  -d '\''{"productIds":[\$PRODUCT_ID]}'\'''
echo ""
if [ ! -z "$PRODUCT_ID" ] && [ ! -z "$TOKEN" ]; then
  ORDER_RESPONSE=$(curl -s -X POST $BASE_URL/api/orders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"productIds\":[$PRODUCT_ID]}")
  echo "$ORDER_RESPONSE"
  ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
  echo "Order ID: $ORDER_ID"
fi
echo ""

echo -e "${BLUE}[ORDERS] Mis órdenes (requiere token)${NC}"
echo "curl $BASE_URL/api/orders"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$TOKEN" ]; then
  curl $BASE_URL/api/orders \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[ORDERS] Ver orden específica${NC}"
echo "curl $BASE_URL/api/orders/\$ORDER_ID"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$ORDER_ID" ]; then
  curl $BASE_URL/api/orders/$ORDER_ID \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[ORDERS] Listar todas las órdenes (Admin)${NC}"
echo "curl $BASE_URL/api/orders/all"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$TOKEN" ]; then
  curl $BASE_URL/api/orders/all \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[ORDERS] Actualizar estado orden${NC}"
echo "curl -X PUT \"$BASE_URL/api/orders/\$ORDER_ID/status?status=SHIPPED\""
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$ORDER_ID" ]; then
  curl -X PUT "$BASE_URL/api/orders/$ORDER_ID/status?status=SHIPPED" \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[ORDERS] Órdenes de un usuario${NC}"
echo "curl $BASE_URL/api/orders/user/1"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$TOKEN" ]; then
  curl $BASE_URL/api/orders/user/1 \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo -e "${BLUE}[PRODUCTS] Eliminar producto${NC}"
echo "curl -X DELETE $BASE_URL/api/products/\$PRODUCT_ID"
echo "  -H \"Authorization: Bearer \$TOKEN\""
echo ""
if [ ! -z "$PRODUCT_ID" ]; then
  curl -X DELETE $BASE_URL/api/products/$PRODUCT_ID \
    -H "Authorization: Bearer $TOKEN"
  echo ""
fi
echo ""

echo "=========================================="
echo "  Fin de las pruebas"
echo "=========================================="
