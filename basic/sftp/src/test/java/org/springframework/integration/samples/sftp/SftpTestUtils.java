package org.springframework.integration.sts;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.integration.handler.ReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Gunnar Hillert
 * @since 2.2
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private Main() { }

	@Autowired
	private StoredProcedureTest storedProcedureTest;

	/**
	 * Load the Spring Integration context and start the process.
	 */
	public static void main(final String... ignored) {

		StringBuilder welcomeMessage = new StringBuilder();
		welcomeMessage.append("\n=========================================================\n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("          Welcome to the Stored Procedure Sample!           \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("    This sample demonstrates how to call four different    \n");
		welcomeMessage.append("    stored procedures:                                      \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("        1. A Simple Stored Procedure Call                 \n");
		welcomeMessage.append("        2. A Stored Procedure Output Parameter              \n");
		welcomeMessage.append("        3. A Stored Procedure Returning a ResultSet         \n");
		welcomeMessage.append("        4. A Stored Procedure with a Poller               \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("=========================================================\n");

		LOGGER.info(welcomeMessage.toString());

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		final StoredProcedureTest storedProcedureTest = context.getBean(StoredProcedureTest.class);

		storedProcedureTest.runStoredProcedureTests();

		LOGGER.info("Hit 'Enter' to terminate");

		final CountDownLatch latch = new CountDownLatch(1);

		Thread terminateThread = new Thread(() -> {
			try {
				System.in.read();
				latch.countDown();
			}
			catch (final IOException e) {
				LOGGER.error("Exception details: {}" , e.getMessage(), e); //added message argument
			}
		});

		terminateThread.start();
		boolean terminated = false;
		try {
			terminated = latch.await(60, TimeUnit.SECONDS); // Timeout after 60 seconds if no Enter key is pressed
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Interrupted while waiting for termination", e);
		}

		Assert.assertTrue("Application terminated", terminated);
		Assert.assertTrue("Stored procedure tests were executed", storedProcedureTest.isStoredProcedureTestsExecuted());

		LOGGER.info("Exiting application. Shutting down context.");
		context.close();

	}

	static class StoredProcedureTest {
		private boolean storedProcedureTestsExecuted;

		public void runStoredProcedureTests() {
			this.storedProcedureTestsExecuted = true;
		}

		public boolean isStoredProcedureTestsExecuted() {
			return this.storedProcedureTestsExecuted;
		}
	}

}
