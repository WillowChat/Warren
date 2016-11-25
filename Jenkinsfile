#!/usr/bin/env groovy

pipeline {
    agent any

    post {
        always {
            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
        }

        success {
            ircSendSuccess()
        }

        failure {
            ircSendFailure()
        }
    }

    stages {
        stage('Checkout') {
            steps {
                ircSendStarted()

                checkout scm
                sh "rm -Rv build || true"
            }
        }

        stage('Build') {
            steps {
                sh "./gradlew clean build -PBUILD_NUMBER=${env.BUILD_NUMBER} --no-daemon"
            }
        }

        stage('Coverage') {
            steps {
                sh "./gradlew jacocoTestReport --no-daemon"

                withCredentials([[$class: 'StringBinding', credentialsId: 'engineer.carrot.warren.warren.codecov', variable: 'CODECOV_TOKEN']]) {
                    sh "./codecov.sh"
                }

                step([$class: 'JacocoPublisher'])
            }
        }

        stage('Archive') {
            steps {
                archive includes: 'build/libs/*.jar'
            }
        }

        if (env.BRANCH_NAME == "develop") {
            stage('Deploy') {
                steps {
                    sh "./gradlew publishMavenJavaPublicationToMavenRepository -PBUILD_NUMBER=${env.BUILD_NUMBER} -PDEPLOY_DIR=/var/www/maven.hopper.bunnies.io --no-daemon"
                }
            }
        }

    }
}