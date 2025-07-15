/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.zip;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Verify that the Spring Integration Application Context starts successfully.
 *
 * @author Gunnar Hillert
 * @author Artem Bilan
 *
 * @since 6.4
 */
class SpringIntegrationProjectStartupTest {

	@Test
	void testStartupOfSpringIntegrationContext() throws Exception{
		try(ConfigurableApplicationContext context
			= new ClassPathXmlApplicationContext("/META-INF/spring/integration/spring-integration-context.xml",
												   getClass())) {
			SpringIntegrationUtils.displayDirectories(context);
			assertThat(context.isRunning()).isTrue();
			assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);
			assertThat(context.containsBean("zipTransformer")).isTrue();
		}
	}

}
