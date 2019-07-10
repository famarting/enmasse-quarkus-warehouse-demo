oc new-project warehouse
oc apply -f deployment_bundle
oc scale deployment --replicas=3 orders-service