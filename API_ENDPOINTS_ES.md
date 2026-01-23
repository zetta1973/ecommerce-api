# Documentación de Endpoints de la API

Esta documentación describe los endpoints disponibles en la API de Ecommerce, incluyendo autenticación, productos, pedidos, usuarios y roles.

## Autenticación

### Registrar Usuario
```
POST /api/auth/register
```

**Descripción**: Registra un nuevo usuario en el sistema.

**Cuerpo de la solicitud**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Respuesta exitosa (201 Created)**:
```json
{
  "message": "User registered successfully"
}
```

---

### Iniciar Sesión
```
POST /api/auth/login
```

**Descripción**: Autentica a un usuario y devuelve un token JWT.

**Cuerpo de la solicitud**:
```json
{
  "usernameOrEmail": "string",
  "password": "string"
}
```

**Respuesta exitosa (200 OK)**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 86400
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

---

### Refrescar Token
```
POST /api/auth/refresh
```

**Descripción**: Genera un nuevo token de acceso usando un token de refresco.

**Cuerpo de la solicitud**:
```json
{
  "refreshToken": "string"
}
```

**Respuesta exitosa (200 OK)**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 86400
}
```

---

## Productos

### Obtener Todos los Productos
```
GET /api/products
```

**Descripción**: Lista todos los productos disponibles.

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
[
  {
    "id": "long",
    "name": "string",
    "description": "string",
    "price": "double",
    "stock": "int"
  }
]
```

---

### Obtener Producto por ID
```
GET /api/products/{id}
```

**Descripción**: Obtiene detalles de un producto específico.

**Parámetros**:
- `id` (path): ID del producto

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "price": "double",
  "stock": "int"
}
```

---

### Crear Producto (Admin)
```
POST /api/products
```

**Descripción**: Crea un nuevo producto (solo para administradores).

**Cuerpo de la solicitud**:
```json
{
  "name": "string",
  "description": "string",
  "price": "double",
  "stock": "int"
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (201 Created)**:
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "price": "double",
  "stock": "int"
}
```

---

### Actualizar Producto (Admin)
```
PUT /api/products/{id}
```

**Descripción**: Actualiza un producto existente (solo para administradores).

**Parámetros**:
- `id` (path): ID del producto

**Cuerpo de la solicitud**:
```json
{
  "name": "string",
  "description": "string",
  "price": "double",
  "stock": "int"
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "price": "double",
  "stock": "int"
}
```

---

### Eliminar Producto (Admin)
```
DELETE /api/products/{id}
```

**Descripción**: Elimina un producto (solo para administradores).

**Parámetros**:
- `id` (path): ID del producto

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (204 No Content)**:

---

## Pedidos

### Crear Pedido
```
POST /api/orders
```

**Descripción**: Crea un nuevo pedido.

**Cuerpo de la solicitud**:
```json
{
  "productIds": ["long"],
  "quantities": ["int"]
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (201 Created)**:
```json
{
  "id": "long",
  "userId": "long",
  "totalAmount": "double",
  "status": "string",
  "createdAt": "timestamp"
}
```

---

### Obtener Pedidos del Usuario
```
GET /api/orders
```

**Descripción**: Lista todos los pedidos del usuario autenticado.

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
[
  {
    "id": "long",
    "userId": "long",
    "totalAmount": "double",
    "status": "string",
    "createdAt": "timestamp"
  }
]
```

---

### Obtener Detalles del Pedido
```
GET /api/orders/{id}
```

**Descripción**: Obtiene detalles de un pedido específico.

**Parámetros**:
- `id` (path): ID del pedido

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
{
  "id": "long",
  "userId": "long",
  "totalAmount": "double",
  "status": "string",
  "createdAt": "timestamp",
  "items": [
    {
      "productId": "long",
      "productName": "string",
      "quantity": "int",
      "price": "double"
    }
  ]
}
```

---

## Administrador

### Obtener Todos los Usuarios (Admin)
```
GET /api/admin/users
```

**Descripción**: Lista todos los usuarios del sistema (solo para administradores).

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
[
  {
    "id": "long",
    "username": "string",
    "email": "string",
    "roles": ["string"]
  }
]
```

---

### Asignar Rol a Usuario (Admin)
```
POST /api/admin/users/{userId}/roles
```

**Descripción**: Asigna un rol a un usuario (solo para administradores).

**Parámetros**:
- `userId` (path): ID del usuario

**Cuerpo de la solicitud**:
```json
{
  "roleName": "string"
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
{
  "message": "Role assigned successfully"
}
```

---

### Administrar Permisos (Admin)
```
POST /api/admin/roles/{roleId}/permissions
```

**Descripción**: Asigna permisos a un rol (solo para administradores).

**Parámetros**:
- `roleId` (path): ID del rol

**Cuerpo de la solicitud**:
```json
{
  "permissionNames": ["string"]
}
```

**Encabezados**:
- `Authorization: Bearer {accessToken}`

**Respuesta exitosa (200 OK)**:
```json
{
  "message": "Permissions assigned successfully"
}
```

---

## Errores Comunes

### 401 Unauthorized
**Descripción**: No se proporcionó un token de autenticación válido.

**Respuesta**:
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

---

### 403 Forbidden
**Descripción**: El usuario no tiene permisos suficientes para acceder al recurso.

**Respuesta**:
```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

---

### 404 Not Found
**Descripción**: El recurso solicitado no existe.

**Respuesta**:
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

---

## Autenticación y Autorización

### Roles
- **USER**: Acceso básico a productos y pedidos.
- **ADMIN**: Acceso completo, incluyendo gestión de usuarios y productos.

### Permisos
Los permisos se asignan a roles y controlan el acceso a endpoints específicos. Ejemplos:
- `product:read`
- `product:write`
- `order:create`
- `user:manage`
- `role:manage`

---

## Documentación Adicional

- **Swagger UI**: Disponible en `/swagger-ui.html` para explorar y probar los endpoints.
- **OpenAPI**: La especificación OpenAPI está disponible en `/v3/api-docs`.
