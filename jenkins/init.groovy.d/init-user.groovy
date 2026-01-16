import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// Crear usuario admin
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin123")  // 👈 Cambia "admin123" si quieres
instance.setSecurityRealm(hudsonRealm)

// Dar permisos de administrador
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
