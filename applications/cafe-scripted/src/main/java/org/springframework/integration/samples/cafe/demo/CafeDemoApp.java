/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.sts;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.service.StringConversionService;

/**
 * Verify that the Spring Integration Application Context starts successfully.
 */

class StringConversionServiceTest {

    @Test
    void testStartupOfSpringIntegrationContext() throws Exception{
        final ApplicationContext context
            = new ClassPathXmlApplicationContext("/META-INF/spring/integration/spring-integration-context.xml",
                                                  StringConversionServiceTest.class);
		Assert.assertNotNull(context);
        Thread.sleep(2000);
		Assert.assertTrue(context.containsBean("stringConversionService"));
    }

    @Test
    void testConvertStringToUpperCase() {
        final ApplicationContext context
            = new ClassPathXmlApplicationContext("/META-INF/spring/integration/spring-integration-context.xml",
                                                  StringConversionServiceTest.class);

        final StringConversionService service = context.getBean(StringConversionService.class);

        final String stringToConvert = "I love Spring Integration";
        final String expectedResult  = "I LOVE SPRING INTEGRATION";

        Assert.assertEquals("Expecting that the string is converted to upper case.",
                expectedResult, service.convertToUpperCase(stringToConvert));
    }

}
