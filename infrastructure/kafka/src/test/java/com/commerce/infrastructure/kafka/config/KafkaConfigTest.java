package com.commerce.infrastructure.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
    }

    @Test
    @DisplayName("ProducerFactory가 올바른 설정으로 생성된다")
    void producerFactory_ShouldBeConfiguredCorrectly() {
        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        assertThat(producerFactory).isNotNull();
        assertThat(producerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);

        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        
        assertThat(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
        assertThat(configs.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configs.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
    }

    @Test
    @DisplayName("KafkaTemplate이 ProducerFactory를 사용하여 생성된다")
    void kafkaTemplate_ShouldBeCreatedWithProducerFactory() {
        // when
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate();

        // then
        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isInstanceOf(DefaultKafkaProducerFactory.class);
    }

    @Test
    @DisplayName("커스텀 bootstrap servers가 설정된다")
    void producerFactory_ShouldUseCustomBootstrapServers() {
        // given
        String customBootstrapServers = "broker1:9092,broker2:9092,broker3:9092";
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", customBootstrapServers);

        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo(customBootstrapServers);
    }

    @Test
    @DisplayName("idempotence가 활성화되어 있다")
    void producerFactory_ShouldHaveIdempotenceEnabled() {
        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
    }

    @Test
    @DisplayName("프로듀서는 모든 in-sync replica의 확인을 기다린다")
    void producerFactory_ShouldWaitForAllReplicasAck() {
        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
    }

    @Test
    @DisplayName("재시도 횟수가 3으로 설정된다")
    void producerFactory_ShouldHaveRetriesSetToThree() {
        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
    }

    @Test
    @DisplayName("직렬화 설정이 올바르게 구성된다")
    void producerFactory_ShouldHaveCorrectSerializers() {
        // when
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // then
        Map<String, Object> configs = ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
    }
}