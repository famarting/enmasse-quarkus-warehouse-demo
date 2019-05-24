#curl -i -d '{"item":"t460s", "quantity":3}' -H "Content-Type: application/json" -X POST http://localhost:8080/stockmoves
curl -i -d '{"item":"t460s", "quantity":3}' -H "Content-Type: application/json" -X POST http://$(oc get routes amqp-cloud-native-app --template='{{ .spec.host }}')/stockmoves

curl -i http://$(oc get routes amqp-cloud-native-app --template='{{ .spec.host }}')/stocks