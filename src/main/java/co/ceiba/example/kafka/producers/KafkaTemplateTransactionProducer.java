package co.ceiba.example.kafka.producers;

import co.ceiba.example.kafka.Example;
import co.ceiba.example.kafka.config.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/kafka-template-transaction", produces = MediaType.APPLICATION_JSON_VALUE)
public class KafkaTemplateTransactionProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTemplateTransactionProducer.class);

    KafkaTemplate<String, Example> producer;

    @Autowired
    public KafkaTemplateTransactionProducer(@Qualifier("kafkaTemplateTransaction") KafkaTemplate<String, Example> producer) {
        this.producer = producer;
    }

    @GetMapping(value = "/{message}/{quantity}")
    public ResponseEntity<?> produce(@PathVariable String message, @PathVariable Integer quantity,
                                                        @RequestParam(defaultValue="1") Integer idFactor ) {

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for(int i = 0; i < quantity; i++){
            String id = (i % idFactor) + "";

            try{
                localTransaction(new Example(id,message), id);
            } catch (IllegalStateException e) {
                //Nothing to do, the IllegalStateException was thrown intentionally to test if messages are sent after an exception
            }
        }

        return ResponseEntity.ok("Messages sent");

    }

    private void localTransaction(@PathVariable Example message, String id) {
        producer.executeInTransaction(t -> {
            Random r = new Random();

            t.send( Topics.KAFKA_TEMPLATE_TRANSACTION, id, message );
            if(r.nextBoolean()){
                LOGGER.error("id " + id + " Shouldn´t be read by the consumer");
                throw new IllegalStateException("Random exception");
            }
            return true;
        });
    }

}
