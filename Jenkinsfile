pipeline {
    agent any

    stages {

        stage('Build & Tests') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean verify'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/*.xml'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'target/*.jar',
                             fingerprint: true
        }

        success {
            echo 'Pipeline CESIZen Back réussi.'
        }

        failure {
            echo 'Pipeline CESIZen Back en échec.'
        }
    }
}