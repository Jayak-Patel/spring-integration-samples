package org.springframework.integration.samples.kafka;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.outbound.KafkaMessageProducer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Value("${kafka.topic}")
	private String topic;

	@Value("${kafka.consumer.group-id}")
	private String consumerGroupId;

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public MessageChannel toKafka() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "toKafka")
	public KafkaMessageProducer kafkaMessageProducer() {
		KafkaMessageProducer producer = new KafkaMessageProducer();
		producer.setTopicExpression(PARSER.parseExpression("'" + this.topic + "'"));
		producer.setProducerFactory(producerFactory());
		return producer;
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> configProps = Collections.singletonMap(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
		DefaultKafkaProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(configProps);
		factory.setKeySerializer(new StringSerializer());
		factory.setValueSerializer(new StringSerializer());
		return factory;
	}

	@Bean
	public KafkaTemplate<Object, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public MessageChannel fromKafka() {
		return new DirectChannel();
	}

	@Bean
	public KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter(
			KafkaMessageListenerContainer<String, String> container) {
		KafkaMessageDrivenChannelAdapter<String, String> adapter = new KafkaMessageDrivenChannelAdapter<>(container);
		adapter.setOutputChannel(fromKafka());
		return adapter;
	}

	@Bean
	public KafkaMessageListenerContainer<String, String> container(ConsumerFactory<String, String> consumerFactory) {
		ContainerProperties properties = new ContainerProperties(this.topic);
		return new KafkaMessageListenerContainer<>(consumerFactory, properties);
	}

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = Collections.singletonMap(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
		DefaultKafkaConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(props);
		factory.setKeyDeserializer(new StringDeserializer());
		factory.setValueDeserializer(new StringDeserializer());
		factory.setConsumerGroupId(this.consumerGroupId);
		return factory;
	}

	@Bean
	@ServiceActivator(inputChannel = "fromKafka")
	public MessageHandler handler() {
		return message -> {
			LOGGER.info(message.getPayload().toString());
		};
	}

	@Bean
	public ApplicationRunner runner(MessageChannel toKafka) {
		return args -> {
			int n = 0;
			while (n++ < 10) {
				toKafka.send(org.springframework.messaging.support.MessageBuilder.withPayload("foo " + n).build());
			}
		};
	}
}
