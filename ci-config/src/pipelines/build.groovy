#!/usr/bin/env groovy

pipeline {
    options {
        timeout(time: 15, unit: 'MINUTES')
    }
    agent any
    // environment {

    // }
    stages {
        stage('test') {
            steps {
                script {
                    hello()
                }
            }
        }
    }
    post {
        always {
            script {
                hello()
            }
        }
    }
}

def hello() {
    println "Hello world"
}