package org.springframework.integration.sts;

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
