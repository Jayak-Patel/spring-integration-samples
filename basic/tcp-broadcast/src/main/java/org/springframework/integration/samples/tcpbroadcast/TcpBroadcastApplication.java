/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.sts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.Socket;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit @Rule that checks that a port is available.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
public class BrokerRunning extends TestWatcher {

	private static final Logger logger = LoggerFactory.getLogger(BrokerRunning.class);

	private final int port;

	private final boolean verify;

	private BrokerRunning(int port, boolean verify) {
		this.port = port;
		this.verify = verify;
	}

	/**
	 * @param port The port.
	 * @return The rule.
	 */
	public static BrokerRunning isRunning(int port) {
		return new BrokerRunning(port, true);
	}

	/**
	 * Do not verify broker actually running.
	 * @param port The port.
	 * @return The rule.
	 */
	public static BrokerRunning isAvailable(int port) {
		return new BrokerRunning(port, false);
	}

	@Override
	protected void starting(Description description) {
		boolean brokerRunning = checkBrokerRunning();
		if (!brokerRunning) {
			throw new AssumptionViolatedException("Broker not running at port " + port);
		}
		if (brokerRunning && this.verify) {
			try {
				// Verify broker is actually running.  Replace with actual broker check.
			}
			catch (Exception e) {
				throw new AssumptionViolatedException("Broker not running at port " + port);
			}
		}
	}

	private boolean checkBrokerRunning() {
		Socket socket = null;
		boolean running = false;
		try {
			socket = new Socket("localhost", port);
			running = true;
		}
		catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Exception while checking broker", e);
			}

		}
		finally {
			running = closeSocket(socket, running);
		}
		return running;
	}

	private boolean closeSocket(Socket socket, boolean running){
		if (socket != null) {
			try {
				socket.close();
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Exception while closing socket", e);
				}
			}
		}
		return running;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface RequiresBrokerRunning {

		int port() default 1883;

	}

}
