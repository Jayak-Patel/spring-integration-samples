package org.springframework.integration.sts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation for tests that require the broker to be running.
 *
 * @author Gary Russell
 * @since 4.0
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MqttBrokerAvailableCondition.class)
public @interface BrokerRunning {
}
