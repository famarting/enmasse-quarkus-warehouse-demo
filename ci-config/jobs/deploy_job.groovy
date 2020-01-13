def folderName = 'deployment'
folder (folderName) {
    description("Continuous deployment jobs")
    displayName("Demo deployment")
}

// pipelineJob("${folderName}/new-commit-prod-branch") {
//     displayName("New deployment because of new commit in prod branch")
    
//     properties {
//         githubProjectUrl('https://github.com/famartinrh/enmasse-quarkus-demo')
//     }
//     logRotator {
//         numToKeep 10
//     }
//     throttleConcurrentBuilds {
//         maxPerNode(1)
//         maxTotal(1)
//     }
//     definition {
//         cps {
//         script(readFileFromWorkspace("ci-config/src/pipelines/deploy.groovy"))
//             sandbox()
//         }
//     }

//     triggers {
//         githubPullRequest {
//             admins(['lulf', 'kornys', 'rgodfrey', 'k-wall'])
//             userWhitelist(['vbusch', 'k-wall', 'lulf', 'kornys', 'robshelly', 'famartinrh', 'ctron', 'grs', 'jbtrystram', 'obabec', 'dejanb', 'zschwarz'])
//             cron('H/2 * * * *')
//             triggerPhrase('.*@enmasse-ci.*(re)?run tests.*')
//             onlyTriggerPhrase()
//             allowMembersOfWhitelistedOrgsAsAdmin()
//             extensions {
//                 commitStatus {
//                     context('Jenkins')
//                     addTestResults(true)
//                     statusUrl('--none--')
//                 }
//             }
//         }
//     }
// }
