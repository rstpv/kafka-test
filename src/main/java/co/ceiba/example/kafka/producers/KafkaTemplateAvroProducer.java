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
@RequestMapping(value = "/kafka-template-avro", produces = MediaType.APPLICATION_JSON_VALUE)
public class KafkaTemplateAvroProducer {

    KafkaTemplate<String, Example> kafkaTemplateAvro;

    @Autowired
    public KafkaTemplateAvroProducer(@Qualifier("kafkaTemplateAvro") KafkaTemplate<String, Example> kafkaTemplateAvro) {
        this.kafkaTemplateAvro = kafkaTemplateAvro;
    }

    @GetMapping(value = "/avro/{message}/{quantity}")
    public CompletableFuture<ResponseEntity<?>> produceAvro(@PathVariable String message, @PathVariable Integer quantity,
                                                            @RequestParam(defaultValue="1") Integer idFactor ) {

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for(int i = 0; i < quantity; i++){
            String id = (i % idFactor) + "";
            Example example = new Example(id, message);
            futures.add(kafkaTemplateAvro.send( Topics.KAFKA_TEMPLATE_AVRO, id ,example ).completable().thenApply(x -> String.format("Sent message[%s] key[%s] topic[%s] partition[%d] timestamp[%d]",
                    example.toString() ,
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
