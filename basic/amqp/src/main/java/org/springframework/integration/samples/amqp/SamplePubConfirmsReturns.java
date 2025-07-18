package org.springframework.integration.sts;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publisher Confirms and Returns configuration.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
@Configuration
public class SamplePubConfirmsReturns {

	/*
	 * When using publisher confirms, the connection factory MUST be configured to
	 * request confirms.
	 */
	@Bean
	public RabbitTemplate template(ConnectionFactory cf) {
		RabbitTemplate template = new RabbitTemplate(cf);
		template.setMandatory(true);
		return template;
	}

	/*
	 * When using returns, the message listener container MUST be configured to return
	 * rejected execution.
	 */
	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory cf) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
		container.setReturnRejected(true);
		return container;
	}

	@Bean
	public Queue queue1() {
		return new Queue("pub.confirms.returns.q1");
	}

}
