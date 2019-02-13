package co.ceiba.example.kafka.consumers;

import co.ceiba.example.kafka.Example;
import co.ceiba.example.kafka.config.Topics;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTemplateAvroConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTemplateConsumer.class);

    @KafkaListener(groupId = "billing", topics = { Topics.KAFKA_TEMPLATE_AVRO }, containerFactory = "avroListenerFactory")
    public void billing(ConsumerRecord<String, Example> record, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        LOGGER.info(
                String.format("Sent message[%s] key[%s] topic[%s] partition[%d] timestamp[%d]",
                        record.value() ,
                        record.key(),
                        Topics.KAFKA_TEMPLATE_AVRO,
                        partition,
                        timestamp) ) ;
    }
}
