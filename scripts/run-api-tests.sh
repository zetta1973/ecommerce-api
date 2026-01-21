#!/bin/bash

# ============================================
# Ecommerce API - Script de pruebas completo
# ============================================
# Uso: bash scripts/run-tests.sh
# ============================================

BASE_URL="http://localhost:8080"
TIMEOUT=5

echo "=========================================="
echo "  Ecommerce API - Pruebas Automáticas"
echo "=========================================="
echo ""

# Función para hacer запрос con curl
make_request() {
    local method=$1
    local endpoint=$2
    local description=$3
    local headers=$4
    local body=$5

    echo -e "\n${BLUE}[$method] $description${NC}"
    echo "Endpoint: $endpoint"

    if [ ! -z "$body" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers \
            -d "$body"
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            $headers
    fi
    echo ""
}

make_request_with_token() {
    local method=$1
    local endpoint=$2
    local description=$3
    local token=$4
    local body=$5

    echo -e "\n${BLUE}[$method] $description${NC}"
    echo "Endpoint: $endpoint"

    if [ ! -z "$body" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "$body"
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token"
    fi
    echo ""
}

echo -e "${GREEN}=== INICIO DE PRUEBAS ===${NC}\n"

# 1. Registro
echo -e "${GREEN}1. REGISTRO DE USUARIO${NC}"
RESPONSE=$(curl -s -X POST $BASE_URL/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","email":"test@test.com","password":"test123"}')
echo "Response: $RESPONSE"
echo ""

# 2. Login
echo -e "${GREEN}2. LOGIN${NC}"
TOKEN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"test123"}')
echo "Response: $TOKEN_RESPONSE"
TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo ""
if [ -z "$TOKEN" ]; then
    echo -e "${YELLOW}ERROR: No se pudo obtener el token${NC}"
    exit 1
fi
echo -e "${GREEN}Token obtido: ${TOKEN:0:50}...${NC}"
echo ""

# 3. Ping público
echo -e "${GREEN}3. PING PÚBLICO${NC}"
curl -s $BASE_URL/admin/ping
echo -e "\n"

# 4. Listar productos (público)
echo -e "${GREEN}4. LISTAR PRODUCTOS (PÚBLICO)${NC}"
curl -s $BASE_URL/api/products
echo -e "\n"

# 5. Buscar productos
echo -e "${GREEN}5. BUSCAR PRODUCTOS${NC}"
curl -s "$BASE_URL/api/products/search?name=test"
echo -e "\n"

# 6. Crear producto
echo -e "${GREEN}6. CREAR PRODUCTO${NC}"
PRODUCT_RESPONSE=$(curl -s -X POST $BASE_URL/api/products \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"Producto Test","description":"Descripción del producto","price":29.99,"stock":100}')
echo "Response: $PRODUCT_RESPONSE"
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Product ID: $PRODUCT_ID"
echo ""

# 7. Ver producto específico
if [ ! -z "$PRODUCT_ID" ]; then
    echo -e "${GREEN}7. VER PRODUCTO $PRODUCT_ID${NC}"
    curl -s $BASE_URL/api/products/$PRODUCT_ID \
        -H "Authorization: Bearer $TOKEN"
    echo -e "\n"
fi

# 8. Ver stock
if [ ! -z "$PRODUCT_ID" ]; then
    echo -e "${GREEN}8. VER STOCK PRODUCTO $PRODUCT_ID${NC}"
    curl -s $BASE_URL/api/products/$PRODUCT_ID/stock \
        -H "Authorization: Bearer $TOKEN"
    echo -e "\n"
fi

# 9. Crear orden
echo -e "${GREEN}9. CREAR ORDEN${NC}"
if [ ! -z "$PRODUCT_ID" ]; then
    ORDER_RESPONSE=$(curl -s -X POST $BASE_URL/api/orders \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"productIds\":[$PRODUCT_ID]}")
    echo "Response: $ORDER_RESPONSE"
    ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Order ID: $ORDER_ID"
    echo ""
fi

# 10. Mis órdenes
echo -e "${GREEN}10. MIS ÓRDENES${NC}"
curl -s $BASE_URL/api/orders \
    -H "Authorization: Bearer $TOKEN"
echo -e "\n"

# 11. Admin - Listar usuarios
echo -e "${GREEN}11. ADMIN - LISTAR USUARIOS${NC}"
curl -s $BASE_URL/admin/users \
    -H "Authorization: Bearer $TOKEN"
echo -e "\n"

# 12. Actualizar orden
if [ ! -z "$ORDER_ID" ]; then
    echo -e "${GREEN}12. ACTUALIZAR ESTADO ORDEN $ORDER_ID${NC}"
    curl -s -X PUT "$BASE_URL/api/orders/$ORDER_ID/status?status=SHIPPED" \
        -H "Authorization: Bearer $TOKEN"
    echo -e "\n"
fi

# 13. Eliminar producto
if [ ! -z "$PRODUCT_ID" ]; then
    echo -e "${GREEN}13. ELIMINAR PRODUCTO $PRODUCT_ID${NC}"
    curl -s -X DELETE $BASE_URL/api/products/$PRODUCT_ID \
        -H "Authorization: Bearer $TOKEN"
    echo -e "\n"
fi

echo "=========================================="
echo -e "${GREEN}=== FIN DE PRUEBAS ===${NC}"
echo "=========================================="
