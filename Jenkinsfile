#!/usr/bin/env groovy

pipeline {
    agent {
        label 'java8'
    }

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

    environment {
        GRADLE_OPTIONS = "--no-daemon --rerun-tasks -PBUILD_NUMBER=${env.BUILD_NUMBER} -PBRANCH=\"${env.BRANCH_NAME}\""
    }

    stages {
        stage('Checkout') {
            steps {
                ircSendStarted()

                checkout scm
                sh "rm -Rv build || true"
            }
        }

        stage('Build & Test') {
            steps {
                sh "./gradlew ${env.GRADLE_OPTIONS} clean build test"
                sh "./gradlew ${env.GRADLE_OPTIONS} generatePomFileForMavenJavaPublication"

                stash includes: 'build/libs/**/*.jar', name: 'build_libs', useDefaultExcludes: false
                stash includes: 'build/publications/mavenJava/pom-default.xml', name: 'maven_artifacts', useDefaultExcludes: false
            }
        }

        stage('Coverage') {
            environment {
                CODECOV_TOKEN = credentials('engineer.carrot.warren.warren.codecov')
            }
            
            steps {
                sh "./gradlew ${env.GRADLE_OPTIONS} jacocoTestReport"

                sh "./codecov.sh -B ${env.BRANCH_NAME}"

                step([$class: 'JacocoPublisher'])
            }
        }

        stage('Archive') {
            steps {
                archive includes: 'build/libs/*.jar'
            }
        }

        stage('Deploy') {
            agent {
                label 'maven_repo'
            }

            steps {
                sh "rm -Rv build || true"
                unstash 'maven_artifacts'
                unstash 'build_libs'
                sh "ls -lR build"

                sh "find build/libs -name Warren\\*${env.BUILD_NUMBER}.jar | head -n 1 | xargs -I '{}' mvn install:install-file -Dfile={} -DpomFile=build/publications/mavenJava/pom-default.xml -DlocalRepositoryPath=/var/www/maven.hopper.bunnies.io"
            }
        }
    }
}