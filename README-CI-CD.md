# 📁 Archivos CI/CD Creados

## Estructura de Archivos

```
ecommerce-api/
├── .gitignore                          # Archivos a ignorar en Git
├── .github/
│   └── workflows/
│       └── ci-cd.yml                   # Workflow de GitHub Actions (CI/CD completo)
├── k8s/
│   ├── deployment.yaml                 # Deployment, Service y HPA base
│   ├── configmap.yaml                  # Configuraciones de la app
│   ├── secrets-example.yaml            # Secret de ejemplo (NO USAR EN PROD)
│   ├── kustomization.yaml              # Configuración de Kustomize
│   └── overlays/
│       ├── staging/
│       │   └── kustomization.yaml      # Overlay para staging
│       └── production/
│           └── kustomization.yaml      # Overlay para production
├── setup-cicd.sh                       # Script de configuración inicial
├── CI-CD-SETUP.md                      # Guía detallada de configuración
└── postman-collection.json            # Colección de pruebas Postman
```

## 🚀 Quick Start

### 1. Ejecutar script de configuración

```bash
# En Linux/Mac (con permisos)
chmod +x setup-cicd.sh
./setup-cicd.sh

# O ejecutar directamente
bash setup-cicd.sh
```

### 2. Manual: Subir a GitHub

```bash
# Inicializar git
git init

# Agregar archivos
git add .

# Commit inicial
git commit -m "feat: configuración CI/CD con GitHub Actions y Kubernetes"

# Agregar remote (reemplaza con tu repo)
git remote add origin https://github.com/TU_USUARIO/ecommerce-api.git

# Push
git branch -M main
git push -u origin main
```

### 3. Configurar Secrets en GitHub

Ve a: `Settings → Secrets and variables → Actions`

Agrega:
- `KUBE_CONFIG`: Contenido de `~/.kube/config` codificado en base64

### 4. Ajustar archivos de configuración

**Importante:** Reemplaza `tu-usuario` en estos archivos con tu nombre de usuario de GitHub:
- `k8s/deployment.yaml` (línea ~17)
- `k8s/kustomization.yaml` (línea ~20)
- `.github/workflows/ci-cd.yml` (líneas ~76, ~120)

## 🔄 Flujo de Trabajo

```
Push a rama 
    ↓
GitHub Actions se ejecuta
    ↓
├─ Build & Test (Maven)
├─ Build Docker Image
└─ Deploy a Kubernetes
    ↓
Despliegue automático según rama:
  - main → production
  - develop → staging
  - feature/* → feature
```

## 🧧 Desplegar con Kustomize

### Desplegar en Staging
```bash
kubectl apply -k k8s/overlays/staging
```

### Desplegar en Production
```bash
kubectl apply -k k8s/overlays/production
```

### Ver cambios
```bash
# Ver estado del deployment
kubectl get deployments -n staging
kubectl get deployments -n production

# Ver pods
kubectl get pods -l app=ecommerce-api -n staging
kubectl get pods -l app=ecommerce-api -n production

# Ver logs
kubectl logs -f deployment/staging-ecommerce-api -n staging
kubectl logs -f deployment/prod-ecommerce-api -n production
```

## 📚 Documentación Detallada

Para información completa, lee: `CI-CD-SETUP.md`

## 🔑 Seguridad Importante

⚠️ **NUNCA commits secrets reales en el repositorio**
- Usa `k8s/secrets-example.yaml` solo como referencia
- Crea tus secrets en Kubernetes con:
  ```bash
  kubectl create secret generic ecommerce-secrets \
    --from-literal=postgres-username=TU_USUARIO \
    --from-literal=postgres-password=TU_PASSWORD \
    --namespace=NAMESPACE
  ```

## ✅ Checklist

Antes del primer despliegue:

- [ ] Repositorio creado en GitHub
- [ ] Script `setup-cicd.sh` ejecutado (o configuración manual completa)
- [ ] Secrets de GitHub configurados
- [ ] KUBE_CONFIG en GitHub Actions
- [] `tu-usuario` reemplazado en archivos de configuración
- [ ] Secrets de Kubernetes creados
- [ ] ConfigMap actualizado con endpoints correctos
- [ ] Permisos de GitHub Packages habilitados
- [ ] Tests pasando localmente (`mvn test`)

## 🆘 Problemas Comunes

### Error: Unauthorized en GitHub Registry
**Solución:**
1. Settings → Actions → General → Workflow permissions
2. Habilitar "Read and write permissions"

### Error: Connection refused en Kubernetes
**Solución:**
1. Verifica KUBE_CONFIG es correcto
2. Asegúrate de que el cluster es accesible desde GitHub Actions

### Pods no inician (CrashLoopBackOff)
**Diagnóstico:**
```bash
kubectl logs deployment/ecommerce-api
kubectl describe pod <pod-name>
```

Más soluciones en: `CI-CD-SETUP.md`

## 📞 Recursos

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Kustomize Docs](https://kustomize.io/)
- [Rancher Docs](https://ranchermanager.docs.rancher.com/)