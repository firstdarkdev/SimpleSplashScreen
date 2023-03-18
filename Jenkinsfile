pipeline {
    agent {
        label "master"
    }
    tools {
        jdk "JAVA17"
    }
    stages {
        stage("Notify Discord") {
            steps {
                discordSend webhookURL: env.FDD_WH_ADMIN,
                        title: "Deploy Started: Simple Splash Screen 1.19.3 Deploy #${BUILD_NUMBER}",
                        link: env.BUILD_URL,
                        result: 'SUCCESS',
                        description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})"
            }
        }
        stage("Prepare") {
            steps {
                sh "wget -O changelog.md https://raw.githubusercontent.com/hypherionmc/changelogs/main/simplesplash/changelog.md"
                sh "chmod +x ./gradlew"
                sh "./gradlew clean"
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publishMod -Prelease=true"
            }
        }
    }
    post {
        always {
            sh "./gradlew --stop"
            deleteDir()

            discordSend webhookURL: env.FDD_WH_ADMIN,
                    title: "Simple Splash Screen 1.19.3 Deploy #${BUILD_NUMBER}",
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})\nStatus: ${currentBuild.currentResult}"
        }
    }
}
