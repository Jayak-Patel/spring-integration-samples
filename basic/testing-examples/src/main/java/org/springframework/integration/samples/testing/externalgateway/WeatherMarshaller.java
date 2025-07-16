package org.springframework.integration.sts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
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

	@Value("${yahoo.weather.uri:http://weather.yahooapis.com/forecastrss?w={code}&u=c}")
	private String yahooUri;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("weatherMarshaller")
	private Marshaller marshaller;

	@Autowired
	@Qualifier("weatherMarshaller")
	private Unmarshaller unmarshaller;

	public Element transform(String code) {
		StreamSource source = this.restTemplate.getForObject(yahooUri, StreamSource.class, code);
		try {
			return (Element) this.unmarshaller.unmarshal(source);
		}
		catch (IOException e) {
			LOGGER.error("Failed to unmarshall", e);
			throw new IllegalStateException("Failed to unmarshall", e);
		}
	}

	public AbstractIntegrationMessageBuilder<?> handleRequest(Message<?> message) {
		String code = (String) message.getPayload();
		StreamSource source = this.restTemplate.getForObject(yahooUri, StreamSource.class, code);
		try {
			Object weather = this.unmarshaller.unmarshal(source);
			Map<String, Object> headers = new HashMap<>();
			headers.put("code", code);
			headers.put("xmlSource", source);
			return MessageBuilder.withPayload(weather)
					.copyHeaders(headers);
		}
		catch (IOException e) {
			LOGGER.error("Failed to unmarshall", e);
			throw new IllegalStateException("Failed to unmarshall", e);
		}
	}

	public Object retransform(Source source) {
		try {
			return this.unmarshaller.unmarshal(source);
		}
		catch (IOException e) {
			LOGGER.error("Failed to unmarshall", e);
			throw new IllegalStateException("Failed to unmarshall", e);
		}
	}

	public Source transformElement(Element element) {
		return new DOMSource(element);
	}

	public WeatherReport transformWeatherReport(WeatherReport report) {
		return report;
	}

}
