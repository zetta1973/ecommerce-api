pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './mvnw clean package -DskipTests'
      }
    }
    stage('Test') {
      steps {
        sh './mvnw test'
      }
    }
    stage('Docker Build') {
      steps {
        sh 'docker build -t ecommerce-api .'
      }
    }
    stage('Deploy') {
      steps {
        sh 'docker-compose up -d'
      }
    }
  }
}
