package org.springframework.integration.sts;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Service for performing string transformations.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
@Component
public class TransformationService {

	private static final Logger LOGGER = LogManager.getLogger();

	private final AtomicInteger counter = new AtomicInteger();

	@Autowired
	private AbstractApplicationContext context;

	@SuppressWarnings("serial")
	public static class DemoException extends RuntimeException {

		public DemoException(String message) {
			super(message);
		}

	}

	public String transform(String input) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("TransformationService was invoked with: " + input);
		}
		if (counter.incrementAndGet() % 3 == 0) {
			throw new DemoException("Demo exception");
		}
		return input.toUpperCase();
	}

	public AbstractRequestHandlerAdvice getChain2Advice() {
		return context.getBean("chain2Advice", AbstractRequestHandlerAdvice.class);
	}

	public Message<?> checkResult(Message<?> message) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Checking the transformed message " + message);
		}
		return message;
	}

}
