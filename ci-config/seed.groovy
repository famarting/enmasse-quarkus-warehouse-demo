def folderName = 'seed'
folder(folderName) {
    displayName("Seed")
}

// job("${folderName}/job-dsl-seed") {
//     description( "job dsl seed" )
//     concurrentBuild(false)
//     steps {
//         dsl {
//             external('**/ci-config/jobs/*.groovy')
//             removeAction('DISABLE')
//         }
//     }
//     triggers {
//         scm('H * * * *')
//     }
//     scm {
//         git {
//             branch("*/master")
//             remote {
//                 url("https://github.com/famartinrh/enmasse-quarkus-demo.git")
//             }
//         }
//     }
// }

job("${folderName}/job-dsl-seed") {
    description( "job dsl seed" )
    concurrentBuild(false)
    customWorkspace('/var/jenkins_home/seed_workspace/')
    steps {
        dsl {
            external('ci-config/jobs/*.groovy')
            removeAction('DISABLE')
        }
    }
}