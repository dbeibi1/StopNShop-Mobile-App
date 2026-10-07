package com.desmondbeibi.stopnshop.session;

import com.desmondbeibi.stopnshop.data.ProductRepository;
import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.CartItem;
import com.desmondbeibi.stopnshop.model.OrderLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.service.CheckoutService;

/**
 * One process-local shopping journey, shared by Android screens through the Application.
 * Use from the main thread. A new process starts a fresh cart and checkout state.
 */
public final class ShoppingSession {
    private final ProductRepository productRepository = new ProductRepository();
    private final Cart cart = new Cart();
    private final CheckoutService checkoutService = new CheckoutService(cart);
    private String checkoutToken;
    private Order lastOrder;
    private List<OrderLine> checkoutLines = Collections.emptyList();

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public Cart getCart() {
        return cart;
    }

    public String beginCheckout() {
        checkoutToken = checkoutService.beginCheckout();
        ArrayList<OrderLine> reviewed = new ArrayList<>();
        for (CartItem item : cart.getItems()) reviewed.add(new OrderLine(item));
        checkoutLines = Collections.unmodifiableList(reviewed);
        return checkoutToken;
    }

    /** Frozen values shown by Checkout; cart edits do not silently change its review. */
    public List<OrderLine> getCheckoutLines() {
        return checkoutLines;
    }

    /** Nullable before the first review; retained after success to support retries. */
    public String getCheckoutToken() {
        return checkoutToken;
    }

    public Order placeDemoOrder(String token, Customer customer) {
        Order order = checkoutService.placeDemoOrder(token, customer);
        // A retry of an older completed token must not replace a newer confirmation.
        if (token.trim().equals(checkoutToken)) {
            lastOrder = order;
        }
        return order;
    }

    /** Nullable before success. The returned immutable order survives cart changes. */
    public Order getLastOrder() {
        return lastOrder;
    }
}
