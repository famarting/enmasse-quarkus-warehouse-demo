CONTAINER_CTL = docker

DEV ?= true

ifeq ($(DEV), true)
CONTAINER_REGISTRY = 172.30.1.1:5000
ORG_NAME = default
else
CONTAINER_REGISTRY = quay.io
ORG_NAME = famargon
endif