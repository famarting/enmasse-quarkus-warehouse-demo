def folderName = 'enmasse-quarkus-demo'
folder(folderName) {
    displayName("Enmasse quarkus demo")
}

[
    [name: 'demo-service', pipeline_name: 'build.groovy'],
    [name: 'orders-rest-api', pipeline_name: 'build.groovy'],
    [name: 'orders-service', pipeline_name: 'build.groovy'],
    [name: 'stocks-service', pipeline_name: 'build.groovy'],
].each { Map config ->

    pipelineJob("${folderName}/${config.name}") {
        description "enmasse master"
        disabled(config.disabled == true)
        parameters {
            stringParam('PROJECT', "${config.name}", 'name of the project to build')
        }
        logRotator {
            numToKeep 10
        }
        throttleConcurrentBuilds {
            maxPerNode(1)
            maxTotal(1)
        }
        definition {
            cps {
            script(readFileFromWorkspace("ci-config/src/pipelines/${config.pipeline_name}"))
                sandbox()
            }
        }
    }
}