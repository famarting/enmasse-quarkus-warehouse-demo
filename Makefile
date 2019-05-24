PROJECT_NAME = amqp-cloud-native

CONTAINER_TARGETS = container_tag container_push
CONTAINER_CTL = docker

DEV ?= true

ifeq ($(DEV), true)
CONTAINER_REGISTRY = 172.30.1.1:5000
ORG_NAME = default
else
CONTAINER_REGISTRY = quay.io
ORG_NAME = famargon
endif

TAG ?= latest
PROJECT_TAG_NAME = $(CONTAINER_REGISTRY)/$(ORG_NAME)/$(PROJECT_NAME):$(TAG)

container: build_jar container_build_jvm $(CONTAINER_TARGETS)

container_native: build_native container_build_native $(CONTAINER_TARGETS)

dev:
	mvn compile quarkus:dev

build_jar:
	mvn package

build_native: 
	mvn package -Pnative -Dnative-image.docker-build=true

container_build_jvm:
	$(CONTAINER_CTL) build -f src/main/docker/Dockerfile.jvm -t $(ORG_NAME)-$(PROJECT_NAME) .
	docker images | grep $(ORG_NAME)-$(PROJECT_NAME)

container_build_native:
	$(CONTAINER_CTL) build -f src/main/docker/Dockerfile.native -t $(ORG_NAME)-$(PROJECT_NAME) .
	docker images | grep $(ORG_NAME)-$(PROJECT_NAME)

container_tag:
	$(CONTAINER_CTL) tag $(ORG_NAME)-$(PROJECT_NAME) $(PROJECT_TAG_NAME)

container_push:
	$(CONTAINER_CTL) push $(PROJECT_TAG_NAME)

clean_deployment_bundle:
	rm -rf deployment_bundle

prepare_deployment_bundle:
	mkdir -p deployment_bundle

deployment_bundle: prepare_deployment_bundle
	cp -r template/bundle/. deployment_bundle/
	oc process -f template/deployment-template.yaml -p CONTAINER_IMAGE=$(PROJECT_TAG_NAME) -o yaml > deployment_bundle/app-deployment.yaml

ifeq ($(DEV), true)
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY) -u $(shell oc whoami) -p $(shell oc whoami -t)
else
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY)
endif

.PHONY: login_container_registry clean_deployment_bundle prepare_deployment_bundle deployment_bundle container_build_jvm container_build_native $(CONTAINER_TARGETS) dev build_jar build_native container container_native
