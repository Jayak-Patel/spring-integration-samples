/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.mongodb.util;

import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.client.MongoClients;

/**
 *
 * @author Oleg Zhurakousky
 */
public class DemoUtils {

	public static MongoDatabaseFactory prepareMongoFactory(String... additionalCollectionToDrop) {
		MongoDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(MongoClients.create(), "test");
		MongoTemplate template = new MongoTemplate(mongoDbFactory);
		try {
			template.dropCollection("messages");
			template.dropCollection("data");
			for (String additionalCollection : additionalCollectionToDrop) {
				template.dropCollection(additionalCollection);
			}
		}
		catch (Exception e) {
			throw new CollectionSetupException("Failed to setup MongoDB collections", e);
		}
		return mongoDbFactory;
	}

	@SuppressWarnings("serial")
	public static class CollectionSetupException extends RuntimeException {

		public CollectionSetupException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
