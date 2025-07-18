package org.springframework.integration.sts;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
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

import org.junit.Assert;

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
	static void main(final String... args) {

		StringBuilder welcomeMessage = new StringBuilder();
		welcomeMessage.append("\n=========================================================\n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("          Welcome to the Stored Procedure Sample!           \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("    This sample demonstrates how to call four different    \n");
		welcomeMessage.append("    stored procedures:                                      \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("        1. A Simple Stored Procedure Call                 \n");
		welcomeMessage.append("        2. A Stored Procedure Output Parameter            