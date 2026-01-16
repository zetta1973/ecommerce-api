# Guía de Configuración CI/CD con GitHub Actions y Kubernetes (Rancher)

## 📋 Requisitos Previos

1. Cuenta en GitHub con el repositorio creado
2. Cluster de Kubernetes gestionado con Rancher
3. kubectl instalado en tu máquina local
4. Docker instalado
5. Maven instalado (para tests locales)

## 🚀 Configuración Inicial

### 1. Configurar GitHub Repository

```bash
# Inicializar git en el proyecto
git init

# Agregar archivos
git add .

# Primer commit
git commit -m "Initial commit: CI/CD setup"

# Agregar remote (reemplaza con tu repo)
git remote add origin https://github.com/TU_USUARIO/ecommerce-api.git

# Push a la rama main
git branch -M main
git push -u origin main
```

### 2. Configurar Secrets en GitHub

Ve a tu repositorio en GitHub:
1. Settings → Secrets and variables → Actions
2. Agrega los siguientes secrets:

| Nombre del Secret | Descripción |
|------------------|-------------|
| `KUBE_CONFIG` | Configuración de kubeconfig en base64 |

**Para obtener el kubeconfig:**

```bash
# Si tienes acceso al cluster a través de Rancher CLI
rancher cluster kubeconfig CLUSTER_NAME > kubeconfig.yaml

# O si usas kubectl directo
cat ~/.kube/config

# Codificar en base64
base64 -i kubeconfig.yaml
```

Copia el resultado y pégalo en el secret `KUBE_CONFIG` de GitHub.

### 3. Configurar GitHub Container Registry

El workflow usa GitHub Container Registry (ghcr.io) por defecto. Asegúrate de:

1. Tener permisos para usar GitHub Packages
2. El paquete está en "Settings → Actions → General → Workflow permissions"
3. Habilitar "Read and write permissions"

### 4. Actualizar el archivo `.github/workflows/ci-cd.yml`

Reemplaza `tu-usuario` con tu nombre de usuario de GitHub:

```yaml
# Línea 76 aproximadamente
image: ghcr.io/TU_USUARIO_REPOSITORIO/ecommerce-api:latest

# Y en el job deploy-to-kubernetes (línea 120)
sed -i "s|ghcr.io/tu-usuario/ecommerce-api:latest|ghcr.io/${{ github.repository }}/ecommerce-api:${IMAGE_TAG}|g"
```

## 📁 Estructura de Archivos Kubernetes

```
k8s/
├── deployment.yaml         # Deployment, Service y HPA
├── configmap.yaml          # Configuraciones de la aplicación
└── secrets-example.yaml    # Secret de ejemplo (NO USAR EN PROD)
```

### Configurar Secrets en Kubernetes

**Opción 1: Crear secret desde línea de comandos**

```bash
# Crear secret con credenciales reales
kubectl create secret generic ecommerce-secrets \
  --from-literal=postgres-username=tu_usuario_real \
  --from-literal=postgres-password=tu_password_real \
  --namespace=default

# Verificar
kubectl get secret ecommerce-secrets -o yaml
```

**Opción 2: Usar un gestor de secrets (Recomendado)**

Considera usar:
- **External Secrets Operator**: https://external-secrets.io/
- **HashiCorp Vault**: https://www.vaultproject.io/
- **Sealed Secrets**: https://github.com/bitnami-labs/sealed-secrets

### Configurar ConfigMap

Edita `k8s/configmap.yaml` según tu infraestructura:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ecommerce-config
  namespace: default
data:
  datasource-url: "jdbc:postgresql://TU_POSTGRES_HOST:5432/ecommerce"
  kafka-bootstrap-servers: "TU_KAFKA_HOST:9092"
  # ... más configuraciones
```

## 🔄 Flujo de CI/CD

### Al hacer push a una rama:

1. **Build and Test**
   - Ejecuta todos los tests de Maven
   - Genera reporte de cobertura JaCoCo
   - Sube reporte como artifact

2. **Build Docker Image**
   - Construye imagen Docker
   - Sube a GitHub Container Registry
   - Tag: `branch-name` y `branch-name-commit-sha`

3. **Deploy to Kubernetes**
   - Actualiza manifiestos con nueva imagen
   - Aplica ConfigMaps
   - Aplica Secrets (si no existen)
   - Aplica Deployment
   - Espera que el rollout esté listo

4. **Notify**
   - Notifica el estado del despliegue

### Ramas y Entornos:

| Rama | Entorno Kubernetes | Prefijo Imagen |
|------|-------------------|----------------|
| `main` | production | `latest`, `main-` |
| `develop` | staging | `develop-` |
| `feature/*` | feature-branch | `feature-branch-` |

## 🧪 Probar Localmente

### 1. Tests

```bash
# Ejecutar tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

### 2. Build Docker Local

```bash
# Construir imagen
docker build -t ecommerce-api:local .

# Ejecutar contenedor
docker run -p 8080:8080 ecommerce-api:local
```

### 3. Desplegar en Kubernetes Local (Minikube/Kind)

```bash
# Crear secret
kubectl create secret generic ecommerce-secrets \
  --from-literal=postgres-username=user \
  --from-literal=postgres-password=pass

# Aplicar manifiestos
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml

# Verificar
kubectl get pods -l app=ecommerce-api
kubectl get svc ecommerce-api-service
```

## 🐛 Troubleshooting

### Error: `permission denied while trying to connect to the Docker daemon`

**Solución:**
- Verifica que el workflow tiene permisos de Docker
- Asegúrate de usar `docker/setup-buildx-action`

### Error: `failed to authenticate to registry`

**Solución:**
- Verifica que el secret `GITHUB_TOKEN` está disponible
- Revisa los permisos del paquete en GitHub

### Error: `Unauthorized: authentication required`

**Solución:**
- Configura "Read and write permissions" en Settings → Actions

### Error: `The connection to the server was refused`

**Solución:**
- Verifica que el `KUBE_CONFIG` es correcto
- Asegúrate de que el cluster es accesible desde GitHub Actions runner
- Verifica que la IP del runner tiene acceso al cluster

### Pod no se inicia (CrashLoopBackOff)

**Diagnóstico:**
```bash
# Ver logs del pod
kubectl logs deployment/ecommerce-api

# Ver eventos
kubectl describe pod <pod-name>

# Ver configuración
kubectl get pod <pod-name> -o yaml
```

### Common Issues

1. **Imagen no se actualiza:**
   - Verifica `imagePullPolicy: Always` en deployment.yaml
   - Verifica que la etiqueta de la imagen es correcta

2. **Secrets no se aplican:**
   - Verifica que el secret existe: `kubectl get secret ecommerce-secrets`
   - Verifica referencia correcta en deployment.yaml

3. **Tests fallan:**
   - Ejecuta tests localmente: `mvn test`
   - Verifica dependencias en pom.xml
   - Revisa logs de ejecución en GitHub Actions

## 📊 Monitoreo

### Ver estado del deployment

```bash
# Ver pods
kubectl get pods -l app=ecommerce-api -w

# Ver logs en tiempo real
kubectl logs -f deployment/ecommerce-api

# Ver eventos del deployment
kubectl describe deployment ecommerce-api
```

### Ver logs en GitHub Actions

Ve a la pestaña "Actions" en tu repositorio para ver:
- Logs de compilación
- Logs de tests
- Logs de Docker build
- Logs de despliegue en Kubernetes

## 🔐 Seguridad

### Best Practices

1. **Nunca commits secrets en el repo**
   - Usa Kubernetes secrets o external secrets operator
   - No comites `k8s/secrets/` o archivos con contraseñas

2. **Usa RBAC en Kubernetes**
   - Crea service accounts específicos
   - Aplica principio de menor privilegio

3. **Imágenes firmadas**
   - Considera usar Cosign para firmar imágenes

4. **Scan de vulnerabilidades**
   - Agrega step de Trivy o Snyk en el pipeline

### Ejemplo de scan de vulnerabilidades

```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.REGISTRY }}/${{ github.repository }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
    format: 'sarif'
    output: 'trivy-results.sarif'

- name: Upload Trivy results to GitHub Security
  uses: github/codeql-action/upload-sarif@v2
  with:
    sarif_file: 'trivy-results.sarif'
```

## 📚 Recursos Adicionales

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Rancher Documentation](https://ranchermanager.docs.rancher.com/)
- [Spring Boot Production](https://spring.io/guides/topicals/spring-boot-production)

## ✅ Checklist Antes del Primer Despliegue

- [ ] Repositorio de GitHub creado
- [ ] Secrets de GitHub configurados
- [ ] KUBE_CONFIG codificado en base64
- [ ] Kubernetes cluster accesible desde GitHub Actions
- [ ] Secrets de Kubernetes creados
- [ ] ConfigMap actualizado con endpoints correctos
- [ ] Dockerfile probado localmente
- [ ] Tests pasando localmente
- [ ] workflow de GitHub Actions actualizado con tu usuario
- [ ] Permisos de GitHub Packages habilitados

## 🎞️ Flujo de Trabajo Recomendado

1. **Desarrollo en branch feature:**
   ```bash
   git checkout -b feature/nueva-funcionalidad
   # ... hacer cambios ...
   git commit -m "feat: añadir nueva funcionalidad"
   git push origin feature/nueva-funcionalidad
   ```

2. **Crear Pull Request a develop:**
   - Tests se ejecutan automáticamente
   - Code review
   - Merge a develop

3. **Despliegue a staging (develop branch):**
   - Imagen se construye
   - Despliegue automático a staging

4. **Validar en staging**

5. **Promover a production:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout main
   git merge develop
   git push origin main
   # Despliegue automático a production
   ```

## 📞 Soporte

Si encuentras problemas, revisa:
1. Logs de GitHub Actions
2. Logs de Kubernetes pods
3. Logs de Rancher
4. Documentación oficial de cada herramienta