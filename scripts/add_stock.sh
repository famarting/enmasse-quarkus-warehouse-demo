#!/bin/bash
curl -i -d '{"item-id":"123456", "quantity":300}' -H "Content-Type: application/json" -X POST http://$(oc get route demo-service --template='{{ .spec.host }}')/warehouse/stocks
echo