pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/AndreyLevoshenya/stage4-module4-task.git'
            }
        }

        stage('Build') {
            steps {

                sh '''
                    echo 'Gradle build'
                    chmod +x gradlew
                     ./gradlew clean build -x test
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube') {
                    echo 'SonarQube analysis'
                    sh './gradlew sonar'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Gradle test'
                sh './gradlew test'
            }
        }

        stage('Docker Build and Deploy') {
            steps {
                sh '''
                    echo "Docker compose build and deploy"
                    docker-compose down
                    docker-compose -f docker-compose-cicd.yml up -d --build
                '''
            }
        }
    }

    post {
        success {
            echo 'Deployment successful'
        }
        failure {
            echo 'Build failed'
        }
        always {
            cleanWs()
        }
    }
}