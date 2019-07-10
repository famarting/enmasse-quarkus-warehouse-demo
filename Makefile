TOPDIR=$(dir $(lastword $(MAKEFILE_LIST)))
include $(TOPDIR)/Makefile.env.mk

SERVICES = orders-rest-api orders-service stocks-service demo-service

container: $(SERVICES) 

$(SERVICES): 
	$(MAKE) -C $@ $(MAKECMDGOALS)

clean_deployment_bundle:
	rm -rf deployment_bundle

prepare_deployment_bundle:
	mkdir -p deployment_bundle

deployment_bundle: $(SERVICES) copy_bundles
	
copy_bundles: clean_deployment_bundle prepare_deployment_bundle
	./scripts/copy_bundles.sh $(SERVICES)

ifeq ($(DEV), true)
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY) -u $(shell oc whoami) -p $(shell oc whoami -t)
else
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY)
endif

.PHONY: login_container_registry clean_deployment_bundle prepare_deployment_bundle deployment_bundle copy_bundles container $(SERVICES)
