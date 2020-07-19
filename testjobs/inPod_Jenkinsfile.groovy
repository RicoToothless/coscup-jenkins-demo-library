library "jenkins-library@master"

inPod(
    containers: [
        containerTemplate(name: 'golang', image: 'golang:1.12-alpine', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'migrate', image: 'migrate/migrate:v4.5.0', ttyEnabled: true, command: 'cat')
    ]
) {
    node(POD_LABEL) {
        try {
            stage('test containers') {
                container('golang') {
                    sh "go version"
                }
                container('migrate') {
                    sh "/migrate -version"
                }
                container('docker') {
                    sh "docker --version"
                }
                container('semver') {
                    sh "semver \"0.0.21\" -r \">0.0.20\""
                }
                container('eks-helm') {
                    sh "kubectl version"
                    sh "aws --version"
                    sh "helm version"
                }
                container('curl') {
                    sh "curl --version"
                }
            }
        } catch (e) {
          currentBuild.result = "FAILED"
          throw e
        }
    }
}
