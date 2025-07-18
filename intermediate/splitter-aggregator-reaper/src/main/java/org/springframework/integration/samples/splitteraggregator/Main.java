package org.springframework.integration.sts;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class CircuitBreakerDemo implements ApplicationRunner {

	private static final Logger LOGGER = LogManager.getLogger();

	@Autowired
	private StatelessClient statelessClient;

	@Autowired
	private ConfigurableApplicationContext context;

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		CountDownLatch latch = context.getBean("cbLatch", CountDownLatch.class);
		for (int i = 0; i < 10; i++) {
			try {
				String result = this.statelessClient.send("hello");
				LOGGER.info("Stateless circuit breaker returned: " + result);
			}
			catch (Exception e) {
				LOGGER.error("Exception during send: " + e.getMessage());
			}
		}
		Assert.isTrue(latch.await(10, TimeUnit.SECONDS), "Latch timed out");
		this.statelessClient.send("goodbye");
	}

	@MessagingGateway
	public interface StatelessClient {

		@Gateway(requestChannel = "circuitBreakerDemo.input")
		String send(String message);

	}

	@Configuration
	@EnableIntegration
	public static class CircuitBreakerConfiguration {

		@Bean
		public MessageChannel circuitBreakerDemoInput() {
			return new DirectChannel();
		}

		@Bean
		public CountDownLatch cbLatch() {
			return new CountDownLatch(3);
		}

		@Bean
		public IntegrationFlow circuitBreakerDemo() {
			SpelExpressionParser parser = new SpelExpressionParser();
			ExpressionEvaluatingRequestHandlerAdvice recoveryAdvice =
					new ExpressionEvaluatingRequestHandlerAdvice();
			recoveryAdvice.setSuccessChannelName("successChannel");
			recoveryAdvice.setExpression("payload + ' - success'");
			recoveryAdvice.setRecoveryExpression("payload + ' - recovery'");
			recoveryAdvice.afterPropertiesSet();
			return f -> f
					.transform(String.class, s -> {
								if (s.equals("goodbye")) {
									throw new RuntimeException("Planned");
								}
								return s;
							},
							e -> e.advice(recoveryAdvice))
					.handle(m -> {
						CountDownLatch latch = m.getHeaders().get("cbLatch", CountDownLatch.class);
						latch.countDown();
						LOGGER.info("Handler received: " + m.getPayload());
					})
					.channel("successChannel")
					.handle(m -> LOGGER.info("Success channel received: " + m.getPayload()));
		}

		@Bean
		public MessageChannel successChannel() {
			return new DirectChannel();
		}

	}

	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
		CircuitBreakerDemo demo = context.getBean(CircuitBreakerDemo.class);
		demo.run(null);
		context.close();
	}

}
