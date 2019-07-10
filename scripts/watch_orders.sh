#!/bin/bash

curl -i http://$(oc get route demo-service --template='{{ .spec.host }}')/warehouse/orders