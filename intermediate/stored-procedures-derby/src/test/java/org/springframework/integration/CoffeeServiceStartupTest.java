/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Verify that the Spring Integration Application Context starts successfully.
 */
public class CoffeeServiceStartupTest {

    @Test
    public void testStartupOfSpringIntegrationContext() throws Exception{
        final ApplicationContext context
            = new ClassPathXmlApplicationContext("/META-INF/spring/integration/spring-integration-context.xml",
                                                  CoffeeServiceStartupTest.class);
        Thread.sleep(2000);
		assertNotNull(context.getBean("integrationEvaluationContextInitializer"));
		assertTrue(context.containsBean("coffeeAggregator"));
    }

}
