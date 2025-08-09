/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.integration.samples.cafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marius Bogoevici
 * @author Tom McCuch
 * @author Gunnar Hillert
 * @author Artem Bilan
 */
public class Delivery implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "-----------------------";

    private transient List<Drink> deliveredDrinks;

    private int orderNumber;

    // Default constructor required by Jackson Java JSON-processor
    public Delivery() {
    }

    public Delivery(List<Drink> deliveredDrinks) {
        this.deliveredDrinks = new ArrayList<>(deliveredDrinks);
        this.orderNumber = deliveredDrinks.get(0).getOrderNumber();
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<Drink> getDeliveredDrinks() {
        return this.deliveredDrinks != null ? new ArrayList<>(this.deliveredDrinks) : null;
    }

    public void setDeliveredDrinks(List<Drink> deliveredDrinks) {
        this.deliveredDrinks = deliveredDrinks != null ? new ArrayList<>(deliveredDrinks) : null;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(SEPARATOR + "\n");
        buffer.append("Order #").append(getOrderNumber()).append("\n");
        List<Drink> drinks = getDeliveredDrinks();
        if (drinks != null) {
            for (Drink drink : drinks) {
                buffer.append(drink);
                buffer.append("\n");
            }
        }
        buffer.append(SEPARATOR + "\n");
        return buffer.toString();
    }

}