package org.springframework.integration.sts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author Artem Bilan
 * @since 4.3
 */
@SpringBootTest
public class ApplicationTests {

	@Autowired
	@Qualifier("inputChannel")
	private MessageChannel inputChannel;

	@Autowired
	@Qualifier("outputChannel")
	private QueueChannel outputChannel;

	@Autowired
	private BarrierDemoApplication.AggregatedGateway aggregatedGateway;

	@Test
	public void testBarrierAggregator() throws InterruptedException {
		int n = 3;
		CountDownLatch countDownLatch = new CountDownLatch(n);
		AtomicInteger counter = new AtomicInteger();

		for (int i = 0; i < n; i++) {
			int index = i;
			new Thread(() -> {
				inputChannel.send(new GenericMessage<>(index));
				counter.incrementAndGet();
				countDownLatch.countDown();
			}).start();
		}

		assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
		assertThat(counter.get()).isEqualTo(n);

		Message<?> receive = outputChannel.receive(10000);
		assertThat(receive).isNotNull();
		Object payload = receive.getPayload();
		assertThat(payload).isInstanceOf(List.class);
		@SuppressWarnings("unchecked")
		List<Integer> list = (List<Integer>) payload;
		assertThat(list).hasSize(n);
		assertThat(list).containsAll(Arrays.asList(0, 1, 2));
	}

	@Test
	public void testAggregatedGateway() {
		List<String> results = aggregatedGateway.echo("Foo", "Bar", "Baz");
		assertThat(results).containsExactly("FOO", "BAR", "BAZ");
	}

}
