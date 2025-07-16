package org.springframework.integration.sts;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.messaging.Message;

import javax.sql.DataSource;

/**
 *
 * @author Gunnar Hillert
 * @since 2.2
 *
 */
public final class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private Main() { }

	/**
	 * Load the Spring Integration context and start the process.
	 */
	public static void main(final String... args) {

		LOGGER.info("\n========================================================="
				+ "\n                                                         "
				+ "\n          Welcome to the Stored Procedure Sample!           "
				+ "\n                                                         "
				+ "\n    This sample demonstrates how to call four different    "
				+ "\n    stored procedures:                                      "
				+ "\n                                                         "
				+ "\n        1. A Simple Stored Procedure Call                 "
				+ "\n        2. A Stored Procedure Output Parameter              "
				+ "\n        3. A Stored Procedure Returning a ResultSet         "
				+ "\n        4. A Stored Procedure with a Poller               "
				+ "\n                                                         "
				+ "\n========================================================="
		);

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		final StoredProcedureTest storedProcedureTest = (StoredProcedureTest) context.getBean("storedProcedureTest");

		storedProcedureTest.runStoredProcedureTests();

		LOGGER.info("Hit 'Enter' to terminate");

		try {
			System.in.read();
		}
		catch (final IOException e) {
			LOGGER.error("Exception details: {}" , e.getMessage(), e);
		}

		LOGGER.info("Exiting application. Shutting down context.");
		context.close();

	}

}
