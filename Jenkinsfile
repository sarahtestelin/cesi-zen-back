pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Tests') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean test'
            }
        }

        stage('Quality & Package') {
            steps {
                sh './mvnw verify -DskipTests'
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