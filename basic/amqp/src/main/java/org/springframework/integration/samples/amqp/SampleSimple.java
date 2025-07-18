package org.springframework.integration.sts;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AMQP simple configuration.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
@Configuration
public class SampleSimple {

	@Bean
	public RabbitTemplate template(ConnectionFactory cf) {
		return new RabbitTemplate(cf);
	}

	@Bean
	public Queue queue1() {
		return new Queue("simple.q1");
	}

}
