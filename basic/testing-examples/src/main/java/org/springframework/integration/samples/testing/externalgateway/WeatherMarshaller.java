/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.springsource.org/spring-integration
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.testing.externalgateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Element;

/**
 * Demonstrates a marshaller/unmarshaller being used to externalize
 * information about weather forecasts for use in an integration flow.
 *
 * @author Gary Russell
 * @since 4.0
 */
@Component
public class WeatherMarshaller {

	private static final Logger LOGGER = LoggerFactory.getLogger(WeatherMarshaller.class);

	private static final String FAILED_TO_UNMARSHALL = "Failed to unmarshall";

	private final String yahooUri;

	private final RestTemplate restTemplate;

	private final Marshaller marshaller;

	private final Unmarshaller unmarshaller;


	public WeatherMarshaller(@Value("${yahoo.weather.uri:http://weather.yahooapis.com/forecastrss?w={code}&u=c}") String yahooUri,
							 RestTemplate restTemplate,
							 @Qualifier("weatherMarshaller") Marshaller marshaller,
							 @Qualifier("weatherMarshaller") Unmarshaller unmarshaller) {
		this.yahooUri = yahooUri;
		this.restTemplate = restTemplate;
		this.marshaller = marshaller;
		this.unmarshaller = unmarshaller;
	}

	public Element transform(String code) {
		StreamSource source = this.restTemplate.getForObject(yahooUri, StreamSource.class, code);
		try {
			return (Element) unmarshaller.unmarshal(source);
		}
		catch (IOException e) {
			String errorMessage = FAILED_TO_UNMARSHALL + " source for code: " + code;
			LOGGER.error(errorMessage, e);
			throw new WeatherMarshallingException(errorMessage, e);
		}
	}

	public AbstractIntegrationMessageBuilder<?> handleRequest(Message<?> message) {
		String code = (String) message.getPayload();
		StreamSource source = restTemplate.getForObject(yahooUri, StreamSource.class, code);
		try {
			Object weather = unmarshaller.unmarshal(source);
			Map<String, Object> headers = new HashMap<>();
			headers.put("code", code);
			headers.put("xmlSource", source);
			return MessageBuilder.withPayload(weather)
					.copyHeaders(headers);
		}
		catch (IOException e) {
			String errorMessage = FAILED_TO_UNMARSHALL + " source for message: " + message;
			LOGGER.error(errorMessage, e);
			throw new WeatherMarshallingException(errorMessage, e);
		}
	}

	public Object retransform(Source source) {
		try {
			return unmarshaller.unmarshal(source);
		}
		catch (IOException e) {
			String errorMessage = FAILED_TO_UNMARSHALL + " source: " + source;
			LOGGER.error(errorMessage, e);
			throw new WeatherMarshallingException(errorMessage, e);
		}
	}

	public Source transformElement(Element element) {
		return new DOMSource(element);
	}

	public WeatherReport transformWeatherReport(WeatherReport report) {
		return report;
	}


	@SuppressWarnings("serial")
	private static class WeatherMarshallingException extends RuntimeException {

		public WeatherMarshallingException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
