# Configuración del Pipeline CI/CD

Este documento describe cómo configurar y mantener el pipeline CI/CD para el proyecto Ecommerce API, incluyendo la configuración de SonarCloud, OWASP Dependency Check, Docker y GitHub Secrets.

## Requisitos Previos

Antes de configurar el pipeline CI/CD, asegúrate de tener lo siguiente:

1. **Cuenta de GitHub**: Con acceso al repositorio del proyecto.
2. **Java 17**: Instalado localmente para desarrollo y pruebas.
3. **Maven**: Versión 3.8.6 o superior.
4. **Docker**: Instalado para construcción y despliegue de imágenes.
5. **SonarCloud Account**: Cuenta en [https://sonarcloud.io](https://sonarcloud.io).

## Configuración Inicial

### 1. Configurar SonarCloud

#### Crear Proyecto en SonarCloud
1. Ve a [https://sonarcloud.io](https://sonarcloud.io) y inicia sesión.
2. Haz clic en "Create new project".
3. Selecciona tu organización (o crea una nueva).
4. Configura el proyecto con los siguientes detalles:
   - **Project Key**: `ecommerce-api`
   - **Display Name**: `Ecommerce API`
5. Sigue las instrucciones para analizar tu proyecto.

#### Generar Token de SonarCloud
1. Ve a tu perfil en SonarCloud (haz clic en tu avatar en la esquina superior derecha).
2. Selecciona "My account" > "Security".
3. Haz clic en "Generate a new token".
4. Proporciona un nombre descriptivo (ej: "GitHub Actions Token").
5. Copia el token generado (¡esto es la única vez que lo verás!).

#### Configurar Token en GitHub
1. Ve a tu repositorio en GitHub.
2. Navega a `Settings` > `Secrets and variables` > `Actions`.
3. Haz clic en "New repository secret".
4. Configura los siguientes secrets:
   - **Name**: `SONAR_TOKEN`
   - **Value**: El token que copiaste de SonarCloud
5. Haz clic en "Add secret".

### 2. Configurar OWASP Dependency Check

#### Obtener Clave API de NVD (Opcional)
OWASP Dependency Check puede actualizar su base de datos de vulnerabilidades usando la API de NVD. Para evitar errores de autenticación, puedes configurar una clave API:

1. Ve a [https://nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key).
2. Solicita una clave API gratuita.
3. Copia la clave API generada.

#### Configurar Clave API en GitHub (Opcional)
Si decides usar una clave API:
1. Ve a `Settings` > `Secrets and variables` > `Actions` en tu repositorio.
2. Haz clic en "New repository secret".
3. Configura:
   - **Name**: `NVD_API_KEY`
   - **Value**: La clave API de NVD
4. Haz clic en "Add secret".

**Nota**: El pipeline está configurado para usar `skipUpdate=true`, por lo que no es necesario configurar la clave API. Esto evita errores de autenticación en entornos CI/CD.

### 3. Configurar Docker

#### Instalar Docker
Asegúrate de tener Docker instalado en tu máquina local:
- **Windows**: Docker Desktop
- **macOS**: Docker Desktop
- **Linux**: Docker Engine

#### Configurar Docker Hub (Opcional)
Si planeas empujar imágenes a Docker Hub:
1. Crea una cuenta en [https://hub.docker.com](https://hub.docker.com).
2. Configura tus credenciales en GitHub:
   - **Name**: `DOCKER_HUB_USERNAME`
   - **Name**: `DOCKER_HUB_TOKEN` (genera un token de acceso en Docker Hub)

**Nota**: Este proyecto usa GitHub Container Registry (GHCR) para almacenar imágenes Docker, por lo que Docker Hub no es necesario.

## Configuración del Pipeline

### Archivos Clave

1. **`.github/workflows/ci-cd-kind.yml`**: Configuración principal del pipeline CI/CD.
2. **`pom.xml`**: Configuración del plugin OWASP Dependency Check.
3. **`sonar-project.properties`**: Configuración de SonarCloud.

### Configuración de `pom.xml`

El plugin OWASP Dependency Check está configurado en `pom.xml`:

```xml
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>9.0.9</version>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <format>HTML</format>
    <failBuildOnCVSS>7</failBuildOnCVSS>
    <skipUpdate>true</skipUpdate>
    <autoUpdate>false</autoUpdate>
    <cveUrlModified>https://services.nvd.nist.gov/rest/json/cves/2.0/?startIndex={0}&resultsPerPage={1}&lastModStartDate={2}&lastModEndDate={3}&apiKey={4}</cveUrlModified>
    <nvdApiDelay>1000</nvdApiDelay>
    <suppressionFiles>
      <suppressionFile>src/main/resources/owasp-suppressions.xml</suppressionFile>
    </suppressionFiles>
  </configuration>
</plugin>
```

**Configuración importante**:
- `skipUpdate=true`: Evita la actualización de la base de datos NVD en CI/CD.
- `failBuildOnCVSS=7`: Falla el build si se detectan vulnerabilidades con CVSS >= 7.
- `nvdApiDelay=1000`: Retraso de 1000ms entre solicitudes a la API de NVD.

### Configuración de `sonar-project.properties`

El archivo `sonar-project.properties` configura el análisis de SonarCloud:

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

**Configuración importante**:
- `sonar.organization`: Reemplaza `zetta1973` con el nombre de tu organización en SonarCloud.
- `sonar.login=${SONAR_TOKEN}`: Usa el token configurado en GitHub Secrets.

## Ejecutar el Pipeline Localmente

### Ejecutar Pruebas
```bash
mvn test
```

### Generar Informe de Cobertura
```bash
mvn jacoco:report
```

### Ejecutar OWASP Dependency Check
```bash
mvn dependency-check:check -DfailBuildOnCVSS=7 -DskipUpdate=true
```

### Construir y Empaquetar
```bash
mvn clean package -DskipTests
```

### Construir Imagen Docker
```bash
docker build -t ecommerce-api:local .
```

### Ejecutar SonarCloud Localmente
```bash
mvn sonar:sonar -Dsonar.login=YOUR_SONAR_TOKEN
```

## Solución de Problemas

### Problemas Comunes

#### 1. Error de Autenticación en SonarCloud
**Síntoma**: `Authentication failed`

**Solución**:
1. Verifica que el token esté correctamente configurado en GitHub Secrets.
2. Asegúrate de que el token no haya caducado.
3. Genera un nuevo token en SonarCloud si es necesario.

#### 2. Error de Actualización de NVD
**Síntoma**: `NVD API returned 403 or 404`

**Solución**:
1. Asegúrate de que `skipUpdate=true` esté configurado en `pom.xml`.
2. Si necesitas actualizar la base de datos localmente, usa:
   ```bash
   mvn dependency-check:update
   ```
3. No es necesario configurar una clave API para entornos CI/CD.

#### 3. Fallas en Pruebas
**Síntoma**: `Tests failed`

**Solución**:
1. Ejecuta las pruebas localmente para identificar el problema:
   ```bash
   mvn test
   ```
2. Revisa los logs de las pruebas para encontrar el error.
3. Corrige el código o actualiza las pruebas según sea necesario.

#### 4. Errores de Docker
**Síntoma**: `Permission denied` o `Cache not found`

**Solución**:
1. Verifica que estés autenticado en GitHub Container Registry:
   ```bash
   echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin
   ```
2. Asegúrate de que el Dockerfile esté correctamente configurado.
3. Verifica que la caché se haya creado en la primera construcción exitosa.

### Registro de Logs

Para depurar problemas, puedes habilitar logs detallados:

```bash
# Logs detallados de Maven
mvn -X test

# Logs detallados de Docker
docker build --progress=plain -t ecommerce-api:local .

# Logs detallados de GitHub Actions
# (Ver logs en la pestaña Actions del repositorio)
```

## Buenas Prácticas

### 1. Gestión de Secrets
- **Nunca commite secrets**: Asegúrate de que los secrets estén configurados en GitHub Secrets y no en el código.
- **Rota secrets regularmente**: Actualiza los tokens cada 3-6 meses.
- **Usa variables de entorno**: Para desarrollo local, usa un archivo `.env` en `.gitignore`.

### 2. Configuración de Calidad
- **SonarCloud**: Configura quality gates para enforzar estándares de calidad.
- **OWASP**: Revisa el informe de vulnerabilidades regularmente.
- **Pruebas**: Mantén una cobertura de pruebas alta (objetivo: 80%+).

### 3. Docker
- **Optimiza capas**: Ordena las capas del Dockerfile de menos a más cambiantes.
- **Usa caché**: Aprovecha la caché de Docker para acelerar las construcciones.
- **Etiquetado**: Usa etiquetas significativas (ej: `v1.0.0`, `latest`).

### 4. CI/CD
- **Pruebas en ramas**: Configura el pipeline para ejecutarse en todas las ramas.
- **Revisión de PRs**: Requiere que el pipeline pase antes de mergear PRs.
- **Notificaciones**: Configura notificaciones para fallas en el pipeline.

## Configuración Avanzada

### Configurar Quality Gates en SonarCloud
1. Ve a tu proyecto en SonarCloud.
2. Haz clic en "Quality Gates".
3. Configura condiciones para fallar el build:
   - Cobertura de pruebas >= 80%
   - Debt Ratio < 0.1
   - Issues críticos = 0

### Configurar Notificaciones
1. Ve a `My Account` > `Notifications` en SonarCloud.
2. Configura notificaciones por email para:
   - Nuevos issues
   - Fallas en quality gates
   - Análisis completados

### Configurar Docker Compose
Si usas Docker Compose para desarrollo local:

```yaml
version: '3.8'

services:
  ecommerce-api:
    image: ecommerce-api:local
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_URL=jdbc:postgresql://postgres:5432/ecommerce
    depends_on:
      - postgres

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin
      - POSTGRES_DB=ecommerce
    ports:
      - "5432:5432"
```

## Recursos Adicionales

- **Documentación de SonarCloud**: [https://docs.sonarcloud.io](https://docs.sonarcloud.io)
- **Documentación de OWASP Dependency Check**: [https://github.com/jeremylong/DependencyCheck](https://github.com/jeremylong/DependencyCheck)
- **Documentación de GitHub Actions**: [https://docs.github.com/es/actions](https://docs.github.com/es/actions)
- **Documentación de Docker**: [https://docs.docker.com](https://docs.docker.com)

## Soporte

Si necesitas ayuda con la configuración del pipeline:

1. Revisa los logs detallados para identificar el problema.
2. Consulta la documentación de los herramientas mencionadas.
3. Revisa los issues similares en el repositorio.
4. Abre un nuevo issue con una descripción detallada del problema.

---

Este documento proporciona una guía comprehensiva para configurar y mantener el pipeline CI/CD del proyecto Ecommerce API. Sigue las instrucciones y buenas prácticas para asegurarte de un proceso CI/CD robusto y seguro.
