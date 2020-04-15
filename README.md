# Warehouse example application with Enmasse and Quarkus

This repository contains an example application for orders processing in a warehouse, with the purpose of trying and showing different technologies. This is a diagram showcasing in high level how the application works

![app diagram](/img/diagram.png)

## How it's implemented?

This example microservices application is implemented combining [quarkus] and [enmasse]. Quarkus is a Java framework used to code the microservices and Enmasse is a platform that runs on top of kubernetes/openshift and provides automatic deployment and management of messaging infrastructure used in this case to provide the queues, topics, and amqp networks used for the communication between the microservices showcased here.

## How to run it?

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