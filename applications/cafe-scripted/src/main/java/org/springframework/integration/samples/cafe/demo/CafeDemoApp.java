/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.cafe.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * An implementation of the Cafe Demo application to demonstrate Spring Integration's
 * scripting capability. This process expects a command line argument corresponding to the scripting language
 * to use.
 *
 * Provides the 'main' method for running the Cafe Demo application. When an
 * order is placed, the Cafe will send that order to the "orders" channel.
 * The relevant components are defined within the configuration file
 * ("cafeDemo.xml").
 *
 * @author Mark Fisher
 * @author Marius Bogoevici
 * @author Oleg Zhurakousky
 * @author David Turanski
 */
public class CafeDemoApp {

    private static final Logger logger = LoggerFactory.getLogger(CafeDemoApp.class);
    private static final String INVALID_COMMAND_LINE_ARGUMENT = "missing or invalid command line argument [groovy,ruby,python]";

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error(INVALID_COMMAND_LINE_ARGUMENT);
            System.exit(1);
        }
        String lang = args[0];

        if (!StringUtils.hasText(lang)){
            logger.error(INVALID_COMMAND_LINE_ARGUMENT);
            System.exit(1);
        }

        lang = lang.toLowerCase();
        if (!isValidLanguage(lang)){
            logger.error(INVALID_COMMAND_LINE_ARGUMENT);
            System.exit(1);
        }

        /*
         * Create an application context and set the active profile to configure the
         * corresponding scripting implementation
         */

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/spring/integration/cafeDemo.xml");
        ctx.getEnvironment().setActiveProfiles(lang);
        ctx.refresh();
    }

    private static boolean isValidLanguage(String lang) {
        return "groovy".equals(lang) || "ruby".equals(lang) || "python".equals(lang);
    }
}