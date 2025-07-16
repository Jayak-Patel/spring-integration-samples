package org.springframework.integration.sts;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"/META-INF/spring/retry-stateless.xml",
		"/META-INF/spring/retry-stateful.xml",
		"/META-INF/spring/retry-circuitbreaker.xml",
		"/META-INF/spring/retry-tx-synch.xml",
		"/META-INF/spring/retry-ftp-delete.xml",
		"/META-INF/spring/retry-ftp-rename-after-failure.xml"})
public class ApplicationConfig {
}
