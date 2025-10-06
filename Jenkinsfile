pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

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
                     ./gradlew clean build
                    cd frontend
                    npm run build
                    cd ..
                    rm -rf module-web/src/main/resources/static/*
                    cp -r frontend/build/* module-web/src/main/resources/static/
                    ./gradlew build
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    echo 'SonarQube analysis'
                    sh './gradlew sonarqube'
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