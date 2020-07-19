import org.yaml.snakeyaml.Yaml
import static ninja.rico.Collections.addWithoutDuplicates

def call(Map args = [:], Closure body) {
    def defaultArgs = [
        serviceAccount: 'jenkins',
        nodeSelector: 'jenkins-slave',
        yaml: '''\
            apiVersion: v1
            kind: Pod
            spec:
              tolerations:
              - key: jenkins
                operator: Equal
                value: slave
                effect: NoSchedule
        '''.stripIndent(),
        containers: [
            containerTemplate(name: 'docker', image: 'docker:19.03.1-dind', ttyEnabled: true, command: 'cat'),
            containerTemplate(name: 'semver', image: 'marcelocorreia/semver:5.5.0', ttyEnabled: true, command: 'cat'),
            containerTemplate(name: 'eks-helm', image: 'alpine/k8s:1.13.12', ttyEnabled: true, command: 'cat'),
            containerTemplate(name: 'curl', image: 'byrnedo/alpine-curl:0.1.8', ttyEnabled: true, command: 'cat')
        ],
        volumes: [
            hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
        ]
    ]

    def finalYaml = args.yaml ? addNodeSelectors(from: defaultArgs.yaml, to: args.yaml) : defaultArgs.yaml
    // For envVars, add the lists together, but remove duplicates by name,
    // giving precedence to the user specified args.
    def finalEnv = addWithoutDuplicates((args.envVars ?: []), defaultArgs.envVars) { it.getArguments().key }
    def finalContainers = addWithoutDuplicates((args.containers ?: []), defaultArgs.containers) { it.getArguments().name }
    def finalVolumes = addWithoutDuplicates((args.volumes ?: []), defaultArgs.volumes) { it.getArguments().mountPath }

    def finalArgs = defaultArgs << args << [yaml: finalYaml, envVars: finalEnv, containers: finalContainers, volumes: finalVolumes]

    podTemplate(finalArgs) {
    body()
    }
}

private def addNodeSelectors(Map args) {
    def yaml = new Yaml()
    def resultMap = (Map) yaml.load(args.to)
    def fromMap = (Map) yaml.load(args.from)

    if (resultMap?.spec) {
        resultMap.spec.affinity = fromMap.spec.affinity
        resultMap.spec.tolerations = (resultMap.spec.tolerations ?: []) + fromMap.spec.tolerations
    } else if (resultMap) {
        resultMap.spec = fromMap.spec
    } else {
        resultMap = fromMap
    }

    yaml.dump(resultMap)
}
