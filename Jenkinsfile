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

        stage('Build & Test') {
            steps {
                parallel(
                    build: {
                        checkout scm
                        sh "rm -Rv build || true"

                        sh "./gradlew clean build -x test -PBUILD_NUMBER=${env.BUILD_NUMBER} -PBRANCH=\"${env.BRANCH_NAME}\" --no-daemon"
                        sh "./gradlew generatePomFileForMavenJavaPublication -PBUILD_NUMBER=${env.BUILD_NUMBER} -PBRANCH=\"${env.BRANCH_NAME}\" --no-daemon"

                        stash includes: 'build/libs/**/*.jar', name: 'build_libs', useDefaultExcludes: false
                        stash includes: 'build/publications/mavenJava/pom-default.xml', name: 'maven_artifacts', useDefaultExcludes: false
                    },
                    test: {
                        checkout scm
                        sh "rm -Rv build || true"

                        sh "./gradlew test -PBUILD_NUMBER=${env.BUILD_NUMBER} -PBRANCH=\"${env.BRANCH_NAME}\" --no-daemon"
                        stash includes: 'build/test-results/**/*', name: 'test_results', useDefaultExcludes: false

                        sh "./gradlew jacocoTestReport --no-daemon"

                        withCredentials([[$class: 'StringBinding', credentialsId: 'engineer.carrot.warren.warren.codecov', variable: 'CODECOV_TOKEN']]) {
                            sh "./codecov.sh -B ${env.BRANCH_NAME}"
                        }

                        step([$class: 'JacocoPublisher'])
                    }
                )
            }
        }

        stage('Archive') {
            steps {
                parallel(
                    archive: {
                        sh "rm -Rv build/libs || true"

                        unstash 'build_libs'

                        sh "ls -lR build/libs"

                        archive includes: 'build/libs/*.jar'
                    },
                    junit: {
                        sh "rm -Rv build/test-results || true"

                        unstash 'test_results'

                        sh "ls -lR build/test-results"

                        junit 'build/test-results/**/*.xml'
                    }
                )
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