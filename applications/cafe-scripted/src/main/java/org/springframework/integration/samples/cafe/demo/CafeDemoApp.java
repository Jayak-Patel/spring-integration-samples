package org.springframework.integration.sts;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.samples.cafe.Order;
import org.springframework.integration.samples.cafe.OrderItem;

/**
 * Simple demo that uses Cafe demo configuration.
 *
 * @author Marius Bogoevici
 * @author Dave Syer
 * @author Gary Russell
 */
public class CafeDemoApp {

	private static volatile boolean running = true;

	public static void main(String[] args) {

		System.out.println("\n========================================================="
				+ "\n                               Welcome to the Cafe Demo!"
				+ "\n    This demo uses Spring Integration to automate the entire cafe process."
				+ "\nFor more information please visit: "
				+ "\nhttps://www.springsource.org/spring-integration"
				+ "\n=========================================================\n");

		final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/integration/cafeDemo.xml");
		context.registerShutdownHook();

		final Cafe cafe = context.getBean("cafe", Cafe.class);

		final Scanner scanner = new Scanner(System.in);

		System.out.print("Please enter your order (enter 'q' to quit):\n");
		int orderNumber = 1;
		while (running) {
			System.out.print("--> ");
			String input = scanner.nextLine();
			if ("q".equals(input.trim())) {
				running = false;
			}
			else {
				Order order = buildOrder(orderNumber++, input);
				try {
					Future<?> future = cafe.placeOrder(order);
					System.out.println("Thanks. Your order #" + order.getNumber() + " is being processed.\n");
					future.get();
				}
				catch (Exception e) {
					System.err.println("There was a problem processing your order: " + e.getMessage());
				}
			}
		}
		System.out.println("Cafe Demo завершена. Thanks for все.");
 		context.close();
		scanner.close();
	}


	private static Order buildOrder(int orderNumber, String input) {
		Order order = new Order(orderNumber);
		List<OrderItem> items = new ArrayList<OrderItem>();
		String[] tokens = input.split(",");
		for (int i = 0; i < tokens.length; i++) {
			String drink = tokens[i].trim();
			items.add(new OrderItem(i + 1, drink, 1, (drink.hashCode() % 2 == 0)));
		}
		order.setItems(items);
		return order;
	}

}
