package co.ceiba.example.kafka.producers;

import co.ceiba.example.kafka.Example;
import co.ceiba.example.kafka.config.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/kafka-template", produces = MediaType.APPLICATION_JSON_VALUE)
public class KafkaTemplateProducer {

    KafkaTemplate<String, String> producer;

    @Autowired
    public KafkaTemplateProducer(KafkaTemplate<String, String> producer, @Qualifier("kafkaTemplateAvro") KafkaTemplate<String, Example> kafkaTemplateAvro) {
        this.producer = producer;
    }

    @GetMapping(value = "/{message}/{quantity}")
    public CompletableFuture<ResponseEntity<?>> produce(@PathVariable String message, @PathVariable Integer quantity,
                                                        @RequestParam(defaultValue="1") Integer idFactor ) {

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for(int i = 0; i < quantity; i++){
            String id = (i % idFactor) + "";
            futures.add(producer.send( Topics.KAFKA_TEMPLATE, id ,message ).completable().thenApply(x -> String.format("Sent message[%s] key[%s] topic[%s] partition[%d] timestamp[%d]",
                    message ,
                    id,
                    x.getProducerRecord().topic(),
                    x.getRecordMetadata().partition(),
                    x.getRecordMetadata().timestamp()) ));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenApply(sentMsgs ->
        {
            List<String> generados = futures.stream()
                    .map(completableFuture -> completableFuture.join())
                    .collect(Collectors.toList());
            return ResponseEntity.ok( generados );
        });
    }

}
