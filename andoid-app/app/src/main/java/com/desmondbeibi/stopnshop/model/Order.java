package com.desmondbeibi.stopnshop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** An immutable completed demo order. It does not process payment or fulfilment. */
public final class Order {
    private final String reference;
    private final Customer customer;
    private final List<OrderLine> lines;
    private final long totalToea;
    private final int totalQuantity;

    public Order(String reference, Customer customer, List<OrderLine> lines) {
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Order reference is required.");
        }
        this.reference = reference.trim();
        this.customer = Objects.requireNonNull(customer, "Customer is required.");
        Objects.requireNonNull(lines, "Order lines are required.");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }

        ArrayList<OrderLine> copy = new ArrayList<>(lines);
        Set<String> productIds = new HashSet<>();
        long total = 0;
        int units = 0;
        for (OrderLine line : copy) {
            Objects.requireNonNull(line, "Order line is required.");
            if (!productIds.add(line.getProductId())) {
                throw new IllegalArgumentException("Order contains duplicate product lines.");
            }
            total = Math.addExact(total, line.getSubtotalToea());
            units = Math.addExact(units, line.getQuantity());
        }
        this.lines = Collections.unmodifiableList(copy);
        totalToea = total;
        totalQuantity = units;
    }

    public String getReference() {
        return reference;
    }

    // Customer is final and immutable, so sharing this value cannot alter the order.
    public Customer getCustomer() {
        return customer;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public long getTotalToea() {
        return totalToea;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }
}
