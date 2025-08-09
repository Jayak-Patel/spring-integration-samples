package org.springframework.integration.samples.mongodb.outbound;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.samples.mongodb.domain.Address;
import org.springframework.integration.samples.mongodb.domain.Person;
import org.springframework.integration.samples.mongodb.util.DemoUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author Oleg Zhurakousky
 * @author Gary Russell
 */
public class MongoDbOutboundAdapterDemo {

    public static void main(String[] args) throws Exception {
        DemoUtils.prepareMongoFactory(); // will clean up MongoDB
        new MongoDbOutboundAdapterDemo().runDefaultAdapter();
    }

    public void runDefaultAdapter() throws Exception {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("mongodb-out-config.xml", MongoDbOutboundAdapterDemo.class)) {
            MessageChannel messageChannel = context.getBean("defaultAdapter", MessageChannel.class);
            messageChannel.send(new GenericMessage<>(createPersonA()));
            messageChannel.send(new GenericMessage<>(createPersonB()));
            messageChannel.send(new GenericMessage<>(createPersonC()));
        }
    }

    private Person createPersonA() {
        Address address = new Address();
        address.setCity("Palo Alto");
        address.setStreet("3401 Hillview Ave");
        address.setZip("94304");
        address.setState("CA");

        Person person = new Person();
        person.setFname("John");
        person.setLname("Doe");
        person.setAddress(address);

        return person;
    }

    private Person createPersonB() {
        Address address = new Address();
        address.setCity("San Francisco");
        address.setStreet("123 Main st");
        address.setZip("94115");
        address.setState("CA");

        Person person = new Person();
        person.setFname("Josh");
        person.setLname("Doe");
        person.setAddress(address);

        return person;
    }

    private Person createPersonC() {
        Address address = new Address();
        address.setCity("Philadelphia");
        address.setStreet("2323 Market st");
        address.setZip("19152");
        address.setState("PA");

        Person person = new Person();
        person.setFname("Jane");
        person.setLname("Doe");
        person.setAddress(address);

        return person;
    }
}