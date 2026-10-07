package com.desmondbeibi.stopnshop.service;

import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.CartItem;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.model.OrderLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Simulated checkout for one session cart.
 * Keep this instance and its token in the future ShoppingSession across screen recreation.
 * Cart mutations and checkout belong on the Android main thread; Cart itself is not thread-safe.
 */
public final class CheckoutService {
    private final Cart cart;
    private final Map<String, Order> completedOrders = new HashMap<>();
    private String pendingToken;
    private List<OrderLine> reviewedLines;

    public CheckoutService(Cart cart) {
        this.cart = Objects.requireNonNull(cart, "Cart is required.");
    }

    /** Reuses the active token when the reviewed cart is unchanged. */
    public synchronized String beginCheckout() {
        List<OrderLine> currentLines = snapshotCart();
        if (currentLines.isEmpty()) {
            throw new IllegalStateException("Add products before checking out.");
        }
        if (pendingToken == null || !currentLines.equals(reviewedLines)) {
            pendingToken = UUID.randomUUID().toString();
            reviewedLines = currentLines;
        }
        return pendingToken;
    }

    /**
     * A completed token always returns its original order, even after shopping starts again.
     * Failed validation never clears the cart or records a completed order.
     */
    public synchronized Order placeDemoOrder(String checkoutToken, Customer customer) {
        String token = requireToken(checkoutToken);
        Objects.requireNonNull(customer, "Customer is required.");
        Order completed = completedOrders.get(token);
        if (completed != null) {
            return completed;
        }
        if (!token.equals(pendingToken)) {
            throw new IllegalArgumentException("Checkout attempt is unknown or expired. Review the cart again.");
        }
        if (!reviewedLines.equals(snapshotCart())) {
            throw new IllegalStateException("Cart changed. Review it again before placing the demo order.");
        }

        Order order = new Order("DEMO-" + UUID.randomUUID(), customer, reviewedLines);
        completedOrders.put(token, order);
        cart.clear();
        pendingToken = null;
        reviewedLines = null;
        return order;
    }

    private List<OrderLine> snapshotCart() {
        ArrayList<OrderLine> snapshot = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            snapshot.add(new OrderLine(item));
        }
        return snapshot;
    }

    private static String requireToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout token is required.");
        }
        return token.trim();
    }
}
