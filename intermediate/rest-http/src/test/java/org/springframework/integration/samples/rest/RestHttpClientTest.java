package org.springframework.integration.sts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Sample REST client.
 *
 * @author Gary Russell
 * @since 3.0
 *
 */
@Component
public class RestHttpClientTest {

	private static final Logger LOGGER = LogManager.getLogger();

	@Autowired
	private RestTemplate restTemplate;

	public Message<?> perform(Message<?> message) {
		String payload = (String) message.getPayload();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Payload = " + payload);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		String result = restTemplate.exchange("http://localhost:8080/receive", HttpMethod.POST, request, String.class)
				.getBody();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result = " + result);
		}
		return MessageBuilder.withPayload(result).copyHeaders(message.getHeaders()).build();
	}

}
