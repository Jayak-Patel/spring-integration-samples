package org.springframework.integration.sts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sample service with methods to demonstrate transaction synchronization.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
@Component
public class TransactionalService {

	private static final Logger LOGGER = LogManager.getLogger();

	@Transactional
	public void transactional(Message<?> message) {
		LOGGER.info("transactional " + message.getPayload());
	}

	public void notTransactional(Message<?> message) {
		LOGGER.info("notTransactional " + message.getPayload());
	}

}
