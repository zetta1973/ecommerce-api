# Ecommerce API - Documentación de Endpoints

## Estado del Deployment
- **Namespace**: ecommerce
- **Deployment**: ecommerce-api
- **Profile**: ci (H2 en memoria)

## Endpoints Públicos (funcionan)

### Auth
```bash
# Registro (funciona)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","email":"email@test.com","password":"test123"}'

# Login (CON PROBLEMAS - requiere revisión)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"email@test.com","password":"test123"}'
```

### Admin
```bash
# Ping (funciona)
curl http://localhost:8080/admin/ping
# Response: pong
```

### Products
```bash
# Listar todos (funciona)
curl http://localhost:8080/api/products

# Buscar por nombre (funciona)
curl "http://localhost:8080/api/products/search?name=producto"
```

## Endpoints Protegidos (requieren JWT)

Los siguientes endpoints requieren el header `Authorization: Bearer <token>`:

### Products
```bash
# Crear producto
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Producto","description":"Desc","price":29.99,"stock":100}'

# Ver producto específico
curl http://localhost:8080/api/products/{id} \
  -H "Authorization: Bearer <token>"

# Ver stock
curl http://localhost:8080/api/products/{id}/stock \
  -H "Authorization: Bearer <token>"

# Actualizar producto
curl -X PUT http://localhost:8080/api/products/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Actualizado","description":"Desc","price":39.99,"stock":150}'

# Eliminar producto
curl -X DELETE http://localhost:8080/api/products/{id} \
  -H "Authorization: Bearer <token>"
```

### Orders
```bash
# Crear orden
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"productIds":[1,2,3]}'

# Mis órdenes
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>"

# Ver orden específica
curl http://localhost:8080/api/orders/{id} \
  -H "Authorization: Bearer <token>"

# Listar todas las órdenes (admin)
curl http://localhost:8080/api/orders/all \
  -H "Authorization: Bearer <token>"

# Actualizar estado
curl -X PUT "http://localhost:8080/api/orders/{id}/status?status=SHIPPED" \
  -H "Authorization: Bearer <token>"

# Órdenes de un usuario
curl http://localhost:8080/api/orders/user/{userId} \
  -H "Authorization: Bearer <token>"
```

### Admin
```bash
# Listar usuarios
curl http://localhost:8080/admin/users \
  -H "Authorization: Bearer <token>"

# Asignar permiso a rol
curl -X POST http://localhost:8080/admin/roles/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"roleName":"ROLE_USER","permissionName":"READ_PRODUCTS"}'
```

## Problema Conocido: Login

El endpoint `/auth/login` actualmente tiene problemas con la autenticación. Esto se debe a un problema de compatibilidad con el PasswordEncoder de Spring Security 6.

### Solución Temporal
Para pruebas locales, se puede:
1. Usar el profile `default` con PostgreSQL
2. Crear usuarios directamente en la base de datos
3. Usar un PasswordEncoder consistente

## Archivos de Colección Postman

- `openapi.yaml` - Especificación OpenAPI 3.0
- `scripts/test-api.sh` - Comandos curl para copiar/pegar
- `scripts/run-api-tests.sh` - Script de pruebas automático

## Deployment en Kubernetes

```bash
# Ver estado del deployment
kubectl get deployment ecommerce-api -n ecommerce

# Ver pods
kubectl get pods -n ecommerce -l app=ecommerce-api

# Ver logs
kubectl logs -n ecommerce -l app=ecommerce-api -f

# Reiniciar deployment
kubectl rollout restart deployment/ecommerce-api -n ecommerce

# Port forward para pruebas locales
kubectl port-forward -n ecommerce svc/ecommerce-api 8080:8080
```
