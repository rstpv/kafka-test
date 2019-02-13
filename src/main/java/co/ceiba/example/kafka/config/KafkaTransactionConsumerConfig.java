package co.ceiba.example.kafka.config;

import co.ceiba.example.kafka.Example;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.requests.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.Locale;
import java.util.Map;

@Configuration
public class KafkaTransactionConsumerConfig {

    @Value("${spring.kafka.schema.registry.url}")
    private String schemaRegistry;

    private final KafkaProperties kafkaProperties;

    public KafkaTransactionConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean("avroTransactionConsumerFactory")
    public ConsumerFactory<String, Example> consumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));

        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);


        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("avroTransactionListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Example> kafkaAvroListenerContainerFactory(
            @Qualifier("transaction-manager") KafkaTransactionManager<Object, Object> kafkaTransactionManager) {
        ConcurrentKafkaListenerContainerFactory<String, Example> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setTransactionManager(kafkaTransactionManager);

        return factory;
    }

}