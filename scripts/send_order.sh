#!/bin/bash
for i in {1..10}
do
    curl -i -d '{"item-id":"123456", "quantity":30}' -H "Content-Type: application/json" -X POST http://$(oc get route orders-rest-api --template='{{ .spec.host }}')/orders
    echo
done