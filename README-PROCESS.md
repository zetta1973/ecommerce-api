🔐 1. Registrar un usuario
curl -X POST http://localhost:30080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "mipass123"
  }'

🔐 2. Iniciar sesión
curl -X POST http://localhost:30080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "mipass123"
  }'
Guarda el token que te devuelve (lo necesitas para los siguientes pasos).

🛒 3. Crear productos (necesitas rol ADMIN)

3.1. Registra un usuario ADMIN
curl -X POST http://localhost:30080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "adminuser",
    "email": "admin@example.com",
    "password": "mipass123"
  }'
3.2. Inicia sesión como ADMIN
curl -X POST http://localhost:30080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "mipass123"
  }'
3.3. Crea productos (usa el token del ADMIN)
TOKEN="TU_TOKEN_DE_ADMIN_AQUI"

curl -X POST http://localhost:30080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Camiseta Deportiva",
    "description": "Camiseta para hacer deporte",
    "price": 29.99,
    "stock": 100
  }'

curl -X POST http://localhost:30080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Zapatillas Running",
    "description": "Zapatillas para correr",
    "price": 89.99,
    "stock": 50
  }'
🛒 4. Ver productos (público, sin token)
curl http://localhost:30080/api/products

📦 5. Crear una orden (necesitas ser usuario registrado)
5.1. Inicia sesión como el usuario normal
curl -X POST http://localhost:30080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "mipass123"
  }'
5.2. Crea una orden (usa el token del usuario normal)
TOKEN="TU_TOKEN_DE_USUARIO_AQUI"

curl -X POST http://localhost:30080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
📊 6. Ver Kafka funcionando
6.1. Accede a KafkaUI
kubectl -n ecommerce port-forward svc/kafka-ui 8082:8080
Luego ve a: http://localhost:8082

6.2. Busca el topic "order.created"
En KafkaUI, busca el topic: order.created
Deberías ver los mensajes que se envían cuando creas una orden.

📝 Resumen del flujo
Registro → /auth/register
Login → /auth/login (obtiene token)
Crear productos → /api/products (con token de ADMIN)
Ver productos → /api/products (público)
Crear orden → /api/orders (con token de usuario)
Ver Kafka → en KafkaUI, topic order.created

🧪 Prueba paso a paso
1. Registra usuarios
bash


1
2
curl -X POST http://localhost:30080/auth/register -H "Content-Type: application/json" -d '{"username":"admin","email":"admin@test.com","password":"mipass123"}'
curl -X POST http://localhost:30080/auth/register -H "Content-Type: application/json" -d '{"username":"user","email":"user@test.com","password":"mipass123"}'
2. Inicia sesión como admin
bash


1
curl -X POST http://localhost:30080/auth/login -H "Content-Type: application/json" -d '{"email":"admin@test.com","password":"mipass123"}'
3. Crea productos
bash


1
2
TOKEN="TOKEN_DE_ADMIN_AQUI"
curl -X POST http://localhost:30080/api/products -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Producto 1","price":10.0,"stock":50}'
4. Inicia sesión como usuario normal
bash


1
curl -X POST http://localhost:30080/auth/login -H "Content-Type: application/json" -d '{"email":"user@test.com","password":"mipass123"}'
5. Crea orden
bash


1
2
TOKEN="TOKEN_DE_USUARIO_AQUI"
curl -X POST http://localhost:30080/api/orders -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}'


profile
Qwen3-Max
12:29 pm
¡Perfecto, Zeta! 😊
Vamos a actualizar ambos controllers para que usen los permisos correctos y estén alineados con tu estructura de seguridad.

✅ 1. OrderController.java actualizado
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
⌄
⌄
⌄
⌄
⌄
⌄
package com.ecommerce.controller;

import com.ecommerce.dto.OrderRequestDto;
import com.ecommerce.dto.OrderResponseDto;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('CREATE_ORDERS')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.status(201).body(orderService.createOrder(user, dto));
    }

    @PreAuthorize("hasAuthority('READ_OWN_ORDERS')")
    @GetMapping
    public List<OrderResponseDto> getUserOrders(@AuthenticationPrincipal User user) {
        return orderService.getUserOrders(user);
    }

    @PreAuthorize("hasAuthority('READ_ALL_ORDERS')")
    @GetMapping("/all")
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PreAuthorize("hasAuthority('UPDATE_ORDER_STATUS')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PreAuthorize("hasAuthority('READ_USER_ORDERS')")
    @GetMapping("/user/{userId}")
    public List<OrderResponseDto> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
}
✅ 2. ProductController.java actualizado
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
⌄
⌄
⌄
⌄
⌄
⌄
⌄
⌄
⌄
⌄
package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repo;

    // Lectura pública de productos
    @GetMapping
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @PreAuthorize("hasAuthority('READ_PRODUCTS')")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('CREATE_PRODUCTS')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = repo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCTS')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updatedProduct = repo.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @PreAuthorize("hasAuthority('DELETE_PRODUCTS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT_STOCK')")
    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> getProductStock(@PathVariable Long id) {
        return repo.findById(id)
            .map(product -> ResponseEntity.ok(product.getStock()))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        // Asegúrate de tener este método en tu repositorio
        return repo.findByNameContainingIgnoreCase(name);
    }
}
✅ 3. Asegúrate de tener el método de búsqueda en ProductRepository.java
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
⌄
⌄
package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmail(String email);
}

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @EntityGraph(attributePaths = {"category"})  // Si tienes categoría
    List<Product> findByNameContainingIgnoreCase(String name);
}
📌 Notas importantes
Sobre permisos:
/api/products GET: público (cualquiera puede ver productos).
/api/products POST/PUT/DELETE: requiere permisos específicos.
/api/orders: todos requieren autenticación y permisos específicos.
Sobre Kafka:
Cuando crees el OrderService, puedes enviar eventos a Kafka después de crear una orden.
Ejemplo: orderProducer.sendOrderCreated(order)


profile
Qwen3-Max
12:47 pm
¡Perfecto, Zeta! 😊
Tu KafkaProducer y KafkaConsumer ya están bien estructurados, pero podemos mejorarlos para que sean más robustos y manejen eventos complejos en lugar de solo IDs.

✅ 1. KafkaProducer.java actualizado
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
⌄
⌄
⌄
⌄
⌄
⌄
package com.ecommerce.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order.created", eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event", e);
        }
    }

    // Método para enviar solo el ID (si lo necesitas)
    public void publishOrderCreated(Long orderId) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setTimestamp(System.currentTimeMillis());
        publishOrderCreated(event);
    }
}

// DTO para el evento
class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private Double total;
    private Long timestamp;
    private String status = "PENDING";

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
✅ 2. KafkaConsumer.java actualizado
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
⌄
⌄
⌄
⌄
⌄
⌄
⌄
package com.ecommerce.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper;

    public KafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.created", groupId = "ecommerce-group")
    public void handleOrderCreated(String eventJson) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(eventJson, OrderCreatedEvent.class);
            
            log.info("📦 Pedido recibido: {}", event.getOrderId());
            log.info("👤 Usuario: {}", event.getUserId());
            log.info("💰 Total: {}", event.getTotal());
            log.info("⏰ Timestamp: {}", event.getTimestamp());
            
            // Simular actualización de inventario
            updateInventory(event);
            
            // Simular envío de notificación
            sendNotification(event);
            
        } catch (Exception e) {
            log.error("Error processing order event: {}", eventJson, e);
        }
    }

    private void updateInventory(OrderCreatedEvent event) {
        log.info("📦 Actualizando inventario para pedido: {}", event.getOrderId());
        // Aquí llamarías a tu servicio de inventario
        // inventoryService.updateStock(event.getOrderId());
    }

    private void sendNotification(OrderCreatedEvent event) {
        log.info("📧 Enviando notificación para pedido: {}", event.getOrderId());
        // Aquí llamarías a tu servicio de notificaciones
        // notificationService.sendOrderConfirmation(event.getUserId(), event.getOrderId());
    }
}
✅ 3. Actualiza tu OrderService.java para usar el nuevo KafkaProducer
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
⌄
public OrderResponseDto createOrder(User user, OrderRequestDto dto) {
    // ... código existente ...

    Order saved = orderRepo.save(order);

    // Enviar evento completo a Kafka
    OrderCreatedEvent event = new OrderCreatedEvent();
    event.setOrderId(saved.getId());
    event.setUserId(user.getId());
    event.setTotal(calculateTotal(products));
    event.setTimestamp(System.currentTimeMillis());
    event.setStatus(saved.getStatus());

    kafkaProducer.publishOrderCreated(event);

    return mapToDto(saved);
}
✅ 4. Asegúrate de tener ObjectMapper en tu Application.java
java


1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
⌄
⌄
⌄
package com.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
🧪 Prueba que Kafka funciona
1. Crea una orden
bash


1
2
3
4
5
6
7
TOKEN="TU_TOKEN_DE_USUARIO_AQUI"
curl -X POST http://localhost:30080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "productIds": [1, 2]
  }'
2. Mira los logs
bash


1
kubectl -n ecommerce logs -l app=ecommerce-api -f
Deberías ver mensajes como:



1
2
3
4
5
📦 Pedido recibido: 1
👤 Usuario: 1
💰 Total: 119.98
📦 Actualizando inventario para pedido: 1


docker build -t ecommerce-api:1.0.5 .
kubectl -n ecommerce rollout restart deployment/ecommerce-api



































COSAS
------------------------------------------
Aquí te presento algunas herramientas clave que complementan y potencian el uso de Docker:
 
1. Docker Compose: 🛠️ Ideal para definir y gestionar aplicaciones multi-contenedor. Simplifica la orquestación y el despliegue de servicios interdependientes.
2. Kubernetes: ☸️ El orquestrador de contenedores por excelencia. Automatiza el despliegue, la escalabilidad y la gestión de aplicaciones en contenedores a gran escala.
3. Portainer: 🚢 Una interfaz gráfica intuitiva para gestionar Docker. Facilita la administración de contenedores, imágenes, redes y volúmenes.
4. Prometheus & Grafana: 📊 Para el monitoreo y la visualización de métricas. Permiten obtener información detallada sobre el rendimiento de tus contenedores y aplicaciones.
5. Traefik: 🚦 Un proxy inverso moderno que automatiza la configuración y el enrutamiento del tráfico hacia tus contenedores.
 
Implementar estas herramientas no solo optimiza tus flujos de trabajo, sino que también mejora la escalabilidad y la resiliencia de tus aplicaciones. 📈
