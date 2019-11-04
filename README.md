# enmasse-quarkus-demo 

This repository shows an example microservices application combining [quarkus] and [enmasse]. Quarkus is a Java framework used to code the microservices and Enmasse is platform that runs on top of kubernetes/openshift and provides automatic deployment and management of messaging infrastructure used in this case to provide the queues, topics, and amqp networks used in the communication between the microservices showcased here.

### Pre-requisistes
The only prerequisite is having an openshift cluster with enmasse installed.
You can find the instructions to install enmasse in the [enmasse docs]

### Package the application
```bash
make login_container_registry
oc new-project warehouse
make container
make deployment_bundle
```
### Deploy messaging infrastructure
```bash
oc apply -f messaging_resources/messaging-resources.yaml
```
Wait for the messaging infrastructure to be ready
```bash
oc get addressspace
oc get address
```
### Deploy the application
```bash
oc apply -f deployment_bundle
```
You can check the application is running successfully
```bash
oc get pod
```
### Quick demo after deploying

```bash
./scripts/scripts-demo/add_stock.sh
./scripts/scripts-demo/send_orders.sh
./scripts/scripts-demo/watch_orders.sh
```

[quarkus]: <https://quarkus.io/>
[enmasse]: <https://enmasse.io/>
[enmasse docs]: <https://enmasse.io/documentation/master/openshift/>