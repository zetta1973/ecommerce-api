# Documentación del Pipeline CI/CD

Este documento proporciona una descripción general del pipeline CI/CD para el proyecto Ecommerce API, incluyendo análisis con SonarCloud, escaneo de dependencias con OWASP, construcción de imágenes Docker y despliegue en GitHub Container Registry.

## Descripción General del Pipeline

El pipeline CI/CD está configurado en `.github/workflows/ci-cd-kind.yml` y se ejecuta en los siguientes eventos:
- Pushes a las ramas `main`, `develop` y `feature/**`
- Pull requests que apuntan a las ramas `main` y `develop`

## Etapas del Pipeline

### 1. Checkout del Código
```yaml
- name: Checkout code
  uses: actions/checkout@v4
```

**Descripción**: Realiza el checkout del código del repositorio usando la acción de checkout de GitHub Actions.

---

### 2. Configurar JDK 17
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
```

**Descripción**: Configura el entorno Java 17 usando la distribución Eclipse Temurin y cachea las dependencias de Maven.

---

### 3. Ejecutar Pruebas
```yaml
- name: Run tests
  run: mvn test
```

**Descripción**: Ejecuta todas las pruebas unitarias e integradas usando Maven.

---

### 4. Generar Informe de Cobertura de Pruebas
```yaml
- name: Generate test coverage report
  run: mvn jacoco:report
```

**Descripción**: Genera un informe de cobertura de pruebas usando JaCoCo y lo guarda en `target/site/jacoco/`.

---

### 5. Escaneo de Dependencias con OWASP
```yaml
- name: OWASP Dependency Check
  run: mvn org.owasp:dependency-check-maven:9.0.9:check -DfailBuildOnCVSS=7 -DskipUpdate=true
```

**Propósito**: Escanea las dependencias del proyecto en busca de vulnerabilidades conocidas.

**Configuración**:
- **Objetivo**: `dependency-check:check` (ejecuta el escaneo de vulnerabilidades)
- **Umbral CVSS**: `7` (falla el build si se detectan vulnerabilidades con puntuación CVSS >= 7)
- **Skip Update**: `true` (usa base de datos local para evitar errores de autenticación con la API de NVD)

**Salida**: Genera un informe de vulnerabilidades en `target/dependency-check-report.html`.

**Nota**: El parámetro `skipUpdate` está configurado como `true` para evitar errores de autenticación con la API de NVD en entornos CI. El plugin usa una base de datos local para la detección de vulnerabilidades. La URL de la API de NVD y el retraso están configurados en `pom.xml` para garantizar la compatibilidad.

---

### 6. Subir Informe OWASP
```yaml
- name: Upload OWASP Report
  uses: actions/upload-artifact@v4
  with:
    name: owasp-report
    path: target/dependency-check-report.html
```

**Descripción**: Sube el informe de OWASP Dependency Check como un artifact del build para su revisión.

---

### 7. Subir Informe de Cobertura
```yaml
- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: coverage-report
    path: target/site/jacoco/
```

**Descripción**: Sube el informe de cobertura de JaCoCo como un artifact del build.

---

### 8. Análisis con SonarCloud
```yaml
- name: SonarCloud Analysis
  uses: SonarSource/sonarcloud-github-action@master
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Propósito**: Realiza análisis estático de código usando SonarCloud.

**Configuración**:
- **GITHUB_TOKEN**: Proporcionado automáticamente por GitHub Actions para acceso al repositorio.
- **SONAR_TOKEN**: Token secreto para autenticación en SonarCloud (debe configurarse en GitHub Secrets).

**Prerrequisitos**:
1. Crea una cuenta en SonarCloud en [https://sonarcloud.io](https://sonarcloud.io).
2. Genera un token en SonarCloud (My Account > Security).
3. Agrega el token como un secreto en GitHub llamado `SONAR_TOKEN`.

**Configuración de SonarCloud**:
El proyecto está configurado en `sonar-project.properties`:
```properties
sonar.projectKey=ecommerce-api
sonar.projectName=Ecommerce API
sonar.organization=zetta1973
sonar.host.url=https://sonarcloud.io
sonar.login=${SONAR_TOKEN}
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.jacoco.reportPath=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

---

### 9. Construir JAR
```yaml
- name: Build JAR
  run: mvn clean package -DskipTests
```

**Descripción**: Construye el proyecto y lo empaqueta en un archivo JAR, saltando las pruebas para ahorrar tiempo.

---

### 10. Verificar JAR
```yaml
- name: Verify JAR exists
  run: ls -la target/*.jar
```

**Descripción**: Verifica que el archivo JAR se haya creado correctamente.

---

### 11. Construir Imagen Docker
```yaml
- name: Build Docker image
  run: docker build -t ecommerce-api:ci --cache-from=ghcr.io/${{ github.repository }}/ecommerce-api:ci --cache-to=type=gha,mode=max .
```

**Propósito**: Construye una imagen Docker para la aplicación.

**Configuración**:
- **Etiqueta de la imagen**: `ecommerce-api:ci`
- **Cache From**: Usa capas cacheadas desde GitHub Container Registry para construcciones más rápidas.
- **Cache To**: Guarda capas en la caché de GitHub Actions para construcciones futuras.

**Beneficios**:
- **Caché**: Reduce el tiempo de construcción reutilizando capas de construcciones anteriores.
- **Eficiencia**: Minimiza el uso de recursos en entornos CI.

---

### 12. Verificar Imagen Docker
```yaml
- name: Verify Docker image
  run: docker images | grep ecommerce-api
```

**Descripción**: Verifica que la imagen Docker se haya construido correctamente.

---

### 13. Iniciar Sesión en GitHub Container Registry
```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

**Descripción**: Autentica con GitHub Container Registry usando el secreto GITHUB_TOKEN.

---

### 14. Etiquetar y Subir Imagen Docker a GHCR
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
- **Etiqueta de la imagen**: `ghcr.io/{repository}/ecommerce-api:ci-{commit-sha}`
- **Ejemplo**: `ghcr.io/your-org/ecommerce-api:ci-1a2b3c4d5e6f789`

**Beneficios**:
- **Trazabilidad**: Cada imagen se etiqueta con el SHA del commit para un seguimiento fácil.
- **Versionado**: Permite el rollback a versiones anteriores si es necesario.

---

### 15. Guardar Imagen Docker como Artifact
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

**Propósito**: Guarda la imagen Docker como un artifact para su descarga manual si es necesario.

**Configuración**:
- **Nombre del artifact**: `ecommerce-api-docker-image`
- **Retención**: 7 días

## Secrets Requeridos

Los siguientes secrets deben configurarse en GitHub Secrets (`Settings` > `Secrets and variables` > `Actions`):

### 1. SONAR_TOKEN
- **Propósito**: Autentica con SonarCloud para análisis estático de código.
- **Cómo obtenerlo**: Genera un token en SonarCloud (My Account > Security).
- **Nota**: Este token es específico de tu cuenta de SonarCloud.

### 2. NVD_API_KEY (Opcional)
- **Propósito**: Usado por OWASP Dependency Check para actualizar la base de datos NVD.
- **Cómo obtenerlo**: Solicita una clave API en [NVD API Key Request](https://nvd.nist.gov/developers/request-an-api-key).
- **Nota**: No es requerido si `skip_update` está configurado como `true` en el plugin de OWASP Dependency Check.

## Artifacts del Build

Los siguientes artifacts se generan y suben para cada build:

1. **Informe OWASP**: `owasp-report` (informe HTML de vulnerabilidades)
2. **Informe de Cobertura**: `coverage-report` (informe de cobertura de JaCoCo)
3. **Imagen Docker**: `ecommerce-api-docker-image` (imagen Docker como `.tar`)

## Condiciones de Falla

El pipeline falla en los siguientes escenarios:

1. **Fallas en Pruebas**: Si alguna prueba unitaria o integrada falla.
2. **Vulnerabilidades CVSS Altas**: Si OWASP Dependency Check detecta vulnerabilidades con puntuación CVSS >= 7.
3. **Issues de SonarCloud**: Si el análisis de SonarCloud detecta issues críticos (configurable en SonarCloud).
4. **Fallas en el Build**: Si el build de Maven o la construcción de la imagen Docker falla.

## Buenas Prácticas

### 1. Configuración de SonarCloud
- Configura quality gates en SonarCloud para enforzar estándares de calidad de código.
- Configura análisis de ramas para monitorear todas las ramas.
- Configura notificaciones para nuevos issues.

### 2. OWASP Dependency Check
- Revisa el informe OWASP regularmente para identificar y corregir vulnerabilidades.
- Usa el parámetro `suppressionFiles` en `pom.xml` para suprimir falsos positivos.
- Mantén la base de datos NVD actualizada localmente para entornos de desarrollo.

### 3. Caché Docker
- El pipeline usa caché de capas Docker para acelerar las construcciones.
- Asegúrate de que tu Dockerfile esté optimizado para caché (ej: ordena capas de menos a más cambiantes).

### 4. Seguridad
- Nunca commite secrets al repositorio.
- Usa GitHub Secrets para información sensible.
- Rota los secrets regularmente.

## Solución de Problemas

### 1. Fallas en Análisis de SonarCloud
- **Error**: `Authentication failed`
  **Solución**: Verifica que `SONAR_TOKEN` esté correctamente configurado en GitHub Secrets.

- **Error**: `Project not found`
  **Solución**: Asegúrate de que `sonar.projectKey` en `sonar-project.properties` coincida con la clave del proyecto en SonarCloud.

### 2. Fallas en OWASP Dependency Check
- **Error**: `NVD API returned 403 or 404`
  **Solución**: Configura `skip_update: true` en el plugin de OWASP Dependency Check para usar la base de datos local.

- **Error**: `No documents exist`
  **Solución**: Asegúrate de que el plugin de OWASP Dependency Check tenga acceso a las dependencias del proyecto.

### 3. Fallas en Construcción Docker
- **Error**: `Permission denied`
  **Solución**: Verifica que el token de GitHub tenga permisos para push a GitHub Container Registry.

- **Error**: `Cache not found`
  **Solución**: La caché se creará en la primera construcción exitosa. Las construcciones posteriores usarán la caché.

### 4. Fallas en Pruebas
- **Error**: `Tests failed`
  **Solución**: Revisa la salida de las pruebas para identificar las pruebas fallidas. Corrige el código o actualiza las pruebas según sea necesario.

## Monitoreo y Notificaciones

- **GitHub Actions**: Visualiza el estado del build y los logs en la pestaña Actions de tu repositorio.
- **SonarCloud**: Monitorea métricas de calidad de código e issues en el dashboard de SonarCloud.
- **Informes OWASP**: Descarga el artifact del informe OWASP para revisar vulnerabilidades.

## Conclusión

Este pipeline CI/CD proporciona una solución comprehensiva para construir, probar y desplegar el proyecto Ecommerce API. Incluye:
- Pruebas automatizadas y análisis de cobertura
- Análisis estático de código con SonarCloud
- Escaneo de vulnerabilidades de dependencias con OWASP
- Construcción y caché de imágenes Docker
- Despliegue a GitHub Container Registry

Siguiendo las buenas prácticas descritas en este documento, puedes asegurarte de un proceso CI/CD robusto y seguro para tu proyecto.
