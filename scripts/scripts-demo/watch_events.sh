#!/bin/bash

curl http://$(oc get route demo-service --template='{{ .spec.host }}')/warehouse/events