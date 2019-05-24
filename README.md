# amqp-cloud-native example application

This repository shows an example application combining [quarkus], smallrye-reactive-messaging-amqp and [enmasse] (to provision messaging infrastructure)
Hence having an openshift cluster with enmasse installed is a requirement.

### Package the application
```bash
make login_container_registry
make container
make deployment_bundle
```
### Deploy messaging infrastructure
```bash
oc new-project stocks-team
oc apply -f deployment_bundle/messaging-resources.yaml
```
Wait for the messaging infrastructure to be ready
```bash
oc get addressspace
```
### Deploy the application
```bash
oc apply -f deployment_bundle/app-deployment.yaml
```
You can check the application is running successfully
```bash
oc get pod
```
### Using the application

1. Send some data
    ```bash
    $ curl -i -d '{"item":"laptopX", "quantity":3}' -H "Content-Type: application/json" -X POST http://$(oc get routes amqp-cloud-native-app --template='{{ .spec.host }}')/stockmoves
    ```
2. Check the data was processed
    ```bash
    $ curl -i http://$(oc get routes amqp-cloud-native-app --template='{{ .spec.host }}')/stocks
    ```

[quarkus]: <https://quarkus.io/>
[enmasse]: <https://enmasse.io/>