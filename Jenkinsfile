pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Tests') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean verify'
            }
        }
    }

    post {
        success {
            echo 'Pipeline CESIZen Back réussi.'
        }

        failure {
            echo 'Pipeline CESIZen Back en échec.'
        }
    }
}