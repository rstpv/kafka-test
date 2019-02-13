package co.ceiba.example.kafka.config;

import co.ceiba.example.kafka.Example;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.Map;

@Configuration
public class KafkaTransactionProducerConfig {

    private final KafkaProperties kafkaProperties;
    @Value("${spring.kafka.schema.registry.url}")
    private String schemaRegistry;

    public KafkaTransactionProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean("transaction-manager")
    public KafkaTransactionManager<?, ?> kafkaTransactionManager(@Qualifier("avroTransactionPF") ProducerFactory<String, Example> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean("avroTransactionPF")
    public ProducerFactory<String, Example> avroProducerFactory() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProperties.getClientId() + "transaction");

        DefaultKafkaProducerFactory<String, Example> stringExampleDefaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(props);
        stringExampleDefaultKafkaProducerFactory.setTransactionIdPrefix("test-transaction");

        return stringExampleDefaultKafkaProducerFactory;
    }

    @Bean("kafkaTemplateTransaction")
    public KafkaTemplate<String, Example> kafkaTemplateTransaction(@Qualifier("avroTransactionPF") ProducerFactory<String, Example> producerFactory ) {
        KafkaTemplate<String, Example> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        return kafkaTemplate;
    }

}
