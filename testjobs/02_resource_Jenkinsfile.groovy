library "jenkins-library@master"

inPod(
    containers: [
        containerTemplate(
            name: 'golang',
            image: 'golang:1.12-alpine',
            ttyEnabled: true,
            command: 'cat',
            resourceRequestMemory: '300Mi',
            resourceLimitMemory: '300Mi'
            )
    ]
) {
    node(POD_LABEL) {
        try {
            stage('sleep') {
                container('golang') {
                    sh "sleep infinity"
                }
            }
        } catch (e) {
          currentBuild.result = "FAILED"
          throw e
        }
    }
}
