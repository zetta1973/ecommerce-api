#!/bin/bash

# Script de configuración inicial para CI/CD con GitHub Actions y Kubernetes

echo "🚀 Iniciando configuración de CI/CD..."
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar prerequisitos
check_prerequisites() {
    echo -e "${YELLOW}Verificando prerequisitos...${NC}"
    
    if ! command -v git &> /dev/null; then
        echo -e "${RED}❌ git no está instalado${NC}"
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}❌ kubectl no está instalado${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisitos verificados${NC}"
    echo ""
}

# Configurar git
setup_git() {
    echo -e "${YELLOW}Configurando Git...${NC}"
    
    if [ ! -d ".git" ]; then
        git init
        echo "Repositorio Git inicializado"
    else
        echo "Repositorio Git ya existe"
    fi
    
    # Pedir URL del repositorio
    read -p "Ingresa la URL de tu repositorio GitHub (https://github.com/usuario/repo.git): " REPO_URL
    
    if [ -n "$REPO_URL" ]; then
        git remote add origin $REPO_URL 2>/dev/null || git remote set-url origin $REPO_URL
        echo "Remote URL configurada: $REPO_URL"
    fi
    
    echo -e "${GREEN}✓ Git configurado${NC}"
    echo ""
}

# Configurar secrets de Kubernetes
setup_kubernetes_secrets() {
    echo -e "${YELLOW}Configurando Secrets de Kubernetes...${NC}"
    
    read -p "Usuario de PostgreSQL: " POSTGRES_USER
    read -sp "Contraseña de PostgreSQL: " POSTGRES_PASS
    echo ""
    
    # Crear secret
    kubectl create secret generic ecommerce-secrets \
        --from-literal=postgres-username=$POSTGRES_USER \
        --from-literal=postgres-password=$POSTGRES_PASS \
        --namespace=default 2>/dev/null && \
        echo -e "${GREEN}✓ Secret creado${NC}" || \
        echo -e "${YELLOW}⚠ Secret ya existe o falló la creación${NC}"
    
    echo ""
}

# Generar kubeconfig para GitHub
setup_kubeconfig() {
    echo -e "${YELLOW}Generando kubeconfig para GitHub Actions...${NC}"
    
    if [ -f "$HOME/.kube/config" ]; then
        cat ~/.kube/config > kubeconfig-temp
        
        echo -e "${GREEN}✓ Kubeconfig generado${NC}"
        echo ""
        echo "Copia el siguiente contenido y pégalo en GitHub como secret 'KUBE_CONFIG':"
        echo "---"
        base64 -i kubeconfig-temp
        echo "---"
        
        # Limpiar
        rm kubeconfig-temp
    else
        echo -e "${RED}❌ No se encontró kubeconfig en ~/.kube/config${NC}"
        echo "Asegúrate de tener acceso al cluster configurado"
    fi
    
    echo ""
}

# Actualizar archivos de configuración
update_config_files() {
    echo -e "${YELLOW}Actualizando archivos de configuración...${NC}"
    
    if [ -n "$REPO_URL" ]; then
        # Extraer usuario del repo URL
        USERNAME=$(echo $REPO_URL | sed -n 's|https://github.com/\([^/]*\)/.*|\1|p')
        
        if [ -n "$USERNAME" ]; then
            # Actualizar deployment.yaml
            sed -i.bak "s|ghcr.io/tu-usuario/ecommerce-api|ghcr.io/$USERNAME/ecommerce-api|g" k8s/deployment.yaml
            rm k8s/deployment.yaml.bak
            echo "Actualizado k8s/deployment.yaml con usuario: $USERNAME"
        fi
    fi
    
    echo -e "${GREEN}✓ Archivos actualizados${NC}"
    echo ""
}

# Commit inicial
initial_commit() {
    echo -e "${YELLOW}Haciendo commit inicial...${NC}"
    
    git add .
    git commit -m "feat: configuración inicial CI/CD con GitHub Actions y Kubernetes"
    
    echo -e "${GREEN}✓ Commit realizado${NC}"
    echo ""
}

# Instrucciones finales
print_instructions() {
    echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ Configuración completada!${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
    echo ""
    echo "📋 Siguientes pasos:"
    echo ""
    echo "1. Configurar secrets en GitHub:"
    echo "   - Ve a tu repositorio en GitHub"
    echo "   - Settings → Secrets and variables → Actions"
    echo "   - Agrega el secret 'KUBE_CONFIG' con el contenido codificado en base64 que mostramos arriba"
    echo ""
    echo "2. Verificar permisos de GitHub Packages:"
    echo "   - Settings → Actions → General → Workflow permissions"
    echo "   - Habilita 'Read and write permissions'"
    echo ""
    echo "3. Push a GitHub:"
    echo "   git branch -M main"
    echo "   git push -u origin main"
    echo ""
    echo "4. Verificar el workflow en GitHub:"
    echo "   - Ve a la pestaña 'Actions'"
    echo "   - Verifica que el pipeline se ejecuta correctamente"
    echo ""
    echo "5. Verificar deployment en Kubernetes:"
    echo "   kubectl get pods -l app=ecommerce-api"
    echo "   kubectl get svc ecommerce-api-service"
    echo ""
    echo "📚 Para más información, consulta: CI-CD-SETUP.md"
    echo ""
}

# Función principal
main() {
    check_prerequisites
    setup_git
    setup_kubernetes_secrets
    setup_kubeconfig
    update_config_files
    initial_commit
    print_instructions
}

# Ejecutar
main