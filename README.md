# kafka-test

## kafka local use one of:
- Docker-compose: `docker-compose up -d` inside `kafka-docker-compose` 
- Confluent cli: https://docs.confluent.io/current/cli/index.html
- https://kafka.apache.org/quickstart


## Rest services
### KafkaTemplateProducer
Sends a plain text message
http://localhost:8080/kafka-template/{message}/{messageCount}?idFactor={idFactor}
- message: Message to be sent
- messageCount: Number of messages to be sent
- idFactor: Each message will have a key= index % idFactor, default to one


## Rest services
### KafkaTemplateAvroProducer
Sends an object serialized with Avro
http://localhost:8080/kafka-template-avro/avro/{message}/{messageCount}?idFactor={idFactor}
- message: Message to be sent
- messageCount: Number of messages to be sent
- idFactor: Each message will have a key= index % idFactor, default to one


## Springcloud kubernetes config

- Create a config-map with the same name than the one in `spring.application.name`
  - If inside the k8s cluster, the kafka broker service is called broker, you can use `kubectl create configmap sample-config --from-file=src/main/resources/application-kubernetes.yml`, if not, modify it accordingly. 
- The user used to execute the container must have permissions to read configmaps.
  - If the default user and default namespace are used, you can use 
  ```
    kubectl create rolebinding default-view \
    --clusterrole=view \
    --serviceaccount=default:default \
    --namespace=default
  ```
