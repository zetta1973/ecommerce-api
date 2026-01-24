# Documentación del Pipeline CI/CD

Este documento proporciona una descripción general del pipeline CI/CD actual para el proyecto Ecommerce API, incluyendo pruebas, análisis con SonarCloud, construcción de imágenes Docker y despliegue en GitHub Container Registry.

## Descripción General del Pipeline

El pipeline CI/CD está configurado en `.github/workflows/ci-cd-kind.yml` y se ejecuta en los siguientes eventos:
- **Pushes** a las ramas `main`, `develop` y `feature/**`
- **Pull requests** que apuntan a las ramas `main` y `develop`

## Etapas del Pipeline

### 1. Checkout del Código
```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0  # Obtener historial completo para SonarCloud
```
Realiza el checkout del repositorio con historial Git completo para análisis de SonarCloud.

### 2. Configurar JDK 17
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
```
Configura el entorno Java 17 usando la distribución Eclipse Temurin y cachea las dependencias de Maven.

### 3. Limpiar Caché de Maven
```yaml
- name: Clean Maven cache
  run: mvn dependency:purge-local-repository
```
Limpia el repositorio local de Maven para garantizar builds frescos.

### 4. Ejecutar Pruebas y Generar Cobertura
```yaml
- name: Run tests and generate coverage
  run: mvn clean verify jacoco:report
```
Ejecuta todas las pruebas unitarias e integradas usando Maven y genera reportes de cobertura JaCoCo.

### 5. Escaneo de Dependencias OWASP (Opcional)
```yaml
- name: OWASP Dependency Check
  run: mvn org.owasp:dependency-check-maven:check
```
**Propósito**: Escanea las dependencias del proyecto en busca de vulnerabilidades conocidas.

### 6. Análisis con SonarCloud
```yaml
- name: SonarCloud Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=zetta1973_ecommerce_api -Dsonar.organization=zetta1973 -Dsonar.host.url=https://sonarcloud.io
```

**Propósito**: Realiza análisis estático de código usando SonarCloud.

**Configuración**:
- **SONAR_TOKEN**: Token secreto para autenticación con SonarCloud
- **Project Key**: `zetta1973_ecommerce_api`
- **Organización**: `zetta1973`
- **Host**: `https://sonarcloud.io`

**Configuración de SonarCloud**:
El proyecto está configurado en `sonar-project.properties`:
```properties
sonar.projectKey=zetta1973_ecommerce_api
sonar.projectName=Ecommerce API
sonar.organization=zetta1973
sonar.host.url=https://sonarcloud.io
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

### 6. Construir JAR
```yaml
- name: Build JAR
  run: mvn clean package -DskipTests
```
Construye el proyecto y lo empaqueta en un archivo JAR ejecutable de Spring Boot.

### 7. Verificar Existencia del JAR
```yaml
- name: Verify JAR exists
  run: ls -la target/*.jar
```
Verifica que el archivo JAR se haya creado exitosamente.

### 8. Construir Imagen Docker
```yaml
- name: Build Docker image
  run: docker build -t ecommerce-api:ci --cache-from=ghcr.io/${{ github.repository }}/ecommerce-api:ci --cache-to=type=gha,mode=max .
```

**Propósito**: Construye una imagen Docker para la aplicación.

**Configuración**:
- **Etiqueta de Imagen**: `ecommerce-api:ci`
- **Cache From**: Usa capas cacheadas desde GitHub Container Registry
- **Cache To**: Guarda capas en la caché de GitHub Actions para builds futuras

### 9. Verificar Imagen Docker
```yaml
- name: Verify Docker image
  run: docker images | grep ecommerce-api
```
Verifica que la imagen Docker se haya construido exitosamente.

### 10. Iniciar Sesión en GitHub Container Registry
```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```
Autentica con GitHub Container Registry usando el GITHUB_TOKEN.

### 11. Etiquetar y Subir Imagen Docker a GHCR
```yaml
- name: Tag and push Docker image to GHCR
  run: |
    IMAGE_TAG=ghcr.io/${{ github.repository }}/ecommerce-api:ci-${{ github.sha }}
    docker tag ecommerce-api:ci $IMAGE_TAG
    docker push $IMAGE_TAG
    echo "Image pushed to: $IMAGE_TAG"
```

**Propósito**: Etiqueta y sube la imagen Docker a GitHub Container Registry.

**Configuración**:
- **Etiqueta de Imagen**: `ghcr.io/{repository}/ecommerce-api:ci-{commit-sha}`
- **Ejemplo**: `ghcr.io/zetta1973/ecommerce-api:ci-1a2b3c4d5e6f789`

### 12. Guardar y Subir Imagen Docker como Artifact
```yaml
- name: Save Docker image as artifact (optional)
  run: docker save -o ecommerce-api.tar ecommerce-api:ci

- name: Upload Docker image artifact
  uses: actions/upload-artifact@v4
  with:
    name: ecommerce-api-docker-image
    path: ecommerce-api.tar
    retention-days: 7
```

**Propósito**: Guarda la imagen Docker como un artifact descargable para inspección manual o despliegue.

## Secrets Requeridos

Los siguientes secrets deben configurarse en GitHub Secrets (`Settings` > `Secrets and variables` > `Actions`):

### 1. SONAR_TOKEN
- **Propósito**: Autentica con SonarCloud para análisis estático de código
- **Cómo obtenerlo**: Genera un token en SonarCloud (My Account > Security)
- **Requerido**: Sí

## Artifacts del Build

Los siguientes artifacts se generan para cada build:

1. **Resultados de Pruebas**: Reportes surefire de Maven
2. **Reporte de Cobertura**: Reportes XML y HTML de JaCoCo
3. **Imagen Docker**: `ecommerce-api-docker-image` (imagen Docker como archivo .tar)
4. **Análisis SonarCloud**: Métricas de calidad de código en dashboard de SonarCloud

## Quality Gates

El pipeline enforcea los siguientes quality gates:

1. **Pruebas**: Todas las pruebas deben pasar
2. **Cobertura de Código**: Generada y analizada por JaCoCo
3. **SonarCloud**: Quality gate debe pasar
4. **Build**: Build de Maven y build de Docker deben tener éxito

## Optimizaciones de Rendimiento

### Caché de Maven
- Las dependencias de Maven se cachean entre builds usando caché de GitHub Actions
- Reduce el tiempo de descarga de dependencias

### Caché de Capas Docker
- Usa GitHub Container Registry para caché de capas
- Caché de GitHub Actions para contexto de build
- Reduce significativamente el tiempo de build de Docker

### Ejecución Paralela
- Los pasos están optimizados para ejecución secuencial donde existen dependencias

## Especificaciones del Entorno

- **Versión Java**: 17 (Temurin)
- **Maven**: Última versión compatible
- **Docker**: Última versión estable
- **Spring Boot**: 3.1.2
- **SonarCloud**: Última versión del scanner
- **Runner**: ubuntu-latest

## Consideraciones de Seguridad

### Gestión de Tokens
- Todos los secrets están almacenados en GitHub Secrets
- Los tokens tienen permisos mínimos requeridos
- Se recomienda rotación regular

### Escaneo de Dependencias
- Disponible a través del análisis de SonarCloud
- No se incluye escaneo OWASP adicional (removido debido a problemas con base de datos H2)

### Seguridad de Contenedores
- Se recomienda escaneo de imagen base antes de despliegue a producción
- Builds multi-etapa para superficie de ataque mínima

## Monitoreo y Observabilidad

### Monitoreo de Build
- GitHub Actions proporciona logs detallados para cada paso
- Estado del build visible en pestaña Actions del repositorio
- Retención de artifacts por 7 días

### Monitoreo de Calidad de Código
- El dashboard de SonarCloud proporciona:
  - Métricas de cobertura de código
  - Deuda técnica
  - Hotspots de seguridad
  - Issues de mantenibilidad

## Solución de Problemas

### Problemas Comunes y Soluciones

1. **Problemas de Conexión con SonarCloud**
   - **Error**: Autenticación fallida o proyecto no encontrado
   - **Solución**: Verificar que SONAR_TOKEN sea correcto y la clave del proyecto coincida con SonarCloud

2. **Fallas en Build de Docker**
   - **Error**: Permiso denegado o fallas en build
   - **Solución**: Revisar sintaxis de Dockerfile y permisos del repositorio

3. **Fallas en Pruebas**
   - **Error**: Pruebas fallando en CI
   - **Solución**: Revisar entorno de pruebas y dependencias

4. **Problemas con Build de Maven**
   - **Error**: Conflictos de dependencias o fallas de compilación
   - **Solución**: Limpiar caché de Maven y revisar versiones de dependencias

## Buenas Prácticas

### Integración con SonarCloud
- Configurar quality gates para estándares de calidad de código
- Configurar análisis de ramas para todas las ramas
- Revisar y corregir issues regularmente

### Optimización Docker
- Ordenar capas de Dockerfile de menos a más frecuentemente cambiantes
- Usar .dockerignore para excluir archivos innecesarios
- Aprovechar builds multi-etapa para imágenes más pequeñas

### Optimización Maven
- Usar gestión de dependencias efectivamente
- Mantener versiones de dependencias actualizadas
- Remover dependencias no utilizadas

## Archivos de Configuración

- **Workflow Principal**: `.github/workflows/ci-cd-kind.yml`
- **Configuración Maven**: `pom.xml`
- **Configuración SonarCloud**: `sonar-project.properties`
- **Configuración Docker**: `Dockerfile`

## Conclusión

Este pipeline CI/CD proporciona una solución comprehensiva para:
- Pruebas automatizadas con reporte de cobertura
- Análisis estático de código y quality gates
- Construcción eficiente de imágenes Docker con caché
- Despliegue automatizado a GitHub Container Registry
- Monitoreo y observabilidad comprehensiva

Siguiendo las prácticas y configuraciones descritas en este documento, aseguras un proceso CI/CD robusto, seguro y eficiente para el proyecto Ecommerce API.