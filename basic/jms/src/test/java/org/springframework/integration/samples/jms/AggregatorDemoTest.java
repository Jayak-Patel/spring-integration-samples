package org.springframework.integration.sts;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.jms.core.JmsTemplate;

/**
 * Main class.
 *
 * @author Gary Russell
 * @author Artem Bilan
 * @since 2.2
 *
 */
public final class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private Main() {
		super();
	}

	public static void main(final String... args) {

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/jms-context.xml");

		context.registerShutdownHook();

		ConnectionFactory cf = context.getBean(ConnectionFactory.class);

		if (cf instanceof ActiveMQConnectionFactory) {
			((ActiveMQConnectionFactory) cf).setTrustAllPackages(true);
		}

		JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

		JmsSendingMessageHandler gateway = new JmsSendingMessageHandler(jmsTemplate);
		gateway.setDefaultDestinationName("queue");

		LOGGER.info("\n========================================================="
				+ "\n                                                         "
				+ "\n          Welcome to Spring Integration JMS Sample     "
				+ "\n                                                         "
				+ "\n    This sample requires a JMS broker. Running the     "
				+ "\n    sample with the default configuration will attempt "
				+ "\n    to connect to a broker running on localhost:61616. "
				+ "\n    See the readme file for instructions on starting    "
				+ "\n    a broker.                                           "
				+ "\n                                                         "
				+ "\n=========================================================" );

		LOGGER.info("Press 'Enter' to exit.");

		try {
			System.in.read();
		}
		catch (final Exception e) {
			LOGGER.error("Exception", e);
		}

		context.close();
	}

}
