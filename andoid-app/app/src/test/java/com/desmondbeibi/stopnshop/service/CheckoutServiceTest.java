package com.desmondbeibi.stopnshop.service;

import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.GroceryProduct;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.model.Product;

import org.junit.Test;

import static org.junit.Assert.*;

public class CheckoutServiceTest {
    @Test
    public void validCheckoutCreatesAccurateDemoOrderThenClearsCart() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        Order order = service.placeDemoOrder(token, customer());
        assertTrue(order.getReference().startsWith("DEMO-"));
        assertEquals(3085L, order.getTotalToea());
        assertEquals(3, order.getTotalQuantity());
        assertEquals(2, order.getLines().size());
        assertEquals("Lae, Morobe Province", order.getCustomer().getLocation());
        assertTrue(cart.isEmpty());
        assertEquals(0L, cart.calculateTotalToea());
    }

    @Test
    public void emptyCartCannotBeginCheckout() {
        Cart cart = new Cart();
        CheckoutService service = new CheckoutService(cart);
        assertThrows(IllegalStateException.class, service::beginCheckout);
        assertThrows(NullPointerException.class, () -> new CheckoutService(null));
        assertTrue(cart.isEmpty());
    }

    @Test
    public void repeatedSubmissionReturnsOriginalOrderAndPreservesNewCart() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        Order order = service.placeDemoOrder(token, customer());
        assertSame(order, service.placeDemoOrder(token, customer()));
        cart.addProduct(oil(), 2);
        Customer editedDetails = new Customer("Changed Demo Name", "76543210", "Different demo location");
        assertSame(order, service.placeDemoOrder(token, editedDetails));
        assertEquals("Demo Shopper", order.getCustomer().getName());
        assertEquals(1390L, cart.calculateTotalToea());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(3085L, order.getTotalToea());
    }

    @Test
    public void newAttemptCreatesAnotherOrderWithoutForgettingEarlierDuplicateProtection() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String firstToken = service.beginCheckout();
        Order first = service.placeDemoOrder(firstToken, customer());
        cart.addProduct(oil(), 1);
        String secondToken = service.beginCheckout();
        Order second = service.placeDemoOrder(secondToken, customer());
        assertNotEquals(firstToken, secondToken);
        assertNotEquals(first.getReference(), second.getReference());
        assertEquals(695L, second.getTotalToea());
        cart.addProduct(oil(), 3);
        assertSame(first, service.placeDemoOrder(firstToken, customer()));
        assertSame(second, service.placeDemoOrder(secondToken, customer()));
        assertEquals(2085L, cart.calculateTotalToea());
    }

    @Test
    public void invalidOrUnknownTokenCannotClearCart() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String validToken = service.beginCheckout();
        for (String token : new String[]{null, " ", "unknown-token"}) {
            assertThrows(IllegalArgumentException.class, () -> service.placeDemoOrder(token, customer()));
        }
        assertEquals(3085L, cart.calculateTotalToea());
        assertEquals(validToken, service.beginCheckout());
    }

    @Test
    public void invalidCustomerCanBeCorrectedWithoutLosingThePendingCart() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        assertThrows(Customer.ValidationException.class,
                () -> service.placeDemoOrder(token, new Customer(" ", "bad", " ")));
        assertThrows(NullPointerException.class, () -> service.placeDemoOrder(token, null));
        assertEquals(3085L, cart.calculateTotalToea());
        assertEquals(token, service.beginCheckout());
        assertEquals(3085L, service.placeDemoOrder(token, customer()).getTotalToea());
        assertTrue(cart.isEmpty());
    }

    @Test
    public void quantityChangeRequiresNewReviewAndExpiresOldPendingToken() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String oldToken = service.beginCheckout();
        cart.setQuantity("chicken", 1);
        assertThrows(IllegalStateException.class, () -> service.placeDemoOrder(oldToken, customer()));
        assertEquals(1890L, cart.calculateTotalToea());
        String newToken = service.beginCheckout();
        assertNotEquals(oldToken, newToken);
        assertThrows(IllegalArgumentException.class, () -> service.placeDemoOrder(oldToken, customer()));
        assertEquals(1890L, service.placeDemoOrder(newToken, customer()).getTotalToea());
    }

    @Test
    public void removingAllItemsAfterReviewCannotCreateAnEmptyOrder() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        cart.clear();
        assertThrows(IllegalStateException.class, () -> service.placeDemoOrder(token, customer()));
        assertThrows(IllegalStateException.class, service::beginCheckout);
        assertTrue(cart.isEmpty());
    }

    @Test
    public void replacingCataloguePriceOrPackAfterReviewRequiresNewReview() {
        Cart cart = new Cart();
        cart.addProduct(oil(), 1);
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        cart.clear();
        cart.addProduct(new GroceryProduct("oil", "Healthy Choice cooking oil", "Groceries",
                "2 L bottle", 995, "Changed test catalogue", false), 1);
        assertThrows(IllegalStateException.class, () -> service.placeDemoOrder(token, customer()));
        assertEquals(995L, cart.calculateTotalToea());
        Order order = service.placeDemoOrder(service.beginCheckout(), customer());
        assertEquals("2 L bottle", order.getLines().get(0).getUnitLabel());
        assertEquals(995L, order.getTotalToea());
    }

    @Test
    public void unchangedReviewReusesTokenIncludingEquivalentProductObjects() {
        Cart cart = sampleCart();
        CheckoutService service = new CheckoutService(cart);
        String token = service.beginCheckout();
        assertEquals(token, service.beginCheckout());
        cart.clear();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        assertEquals(token, service.beginCheckout());
        assertEquals(3085L, service.placeDemoOrder(token, customer()).getTotalToea());
    }

    @Test
    public void tokenFromAnotherServiceCannotSubmitCurrentCart() {
        Cart firstCart = sampleCart();
        CheckoutService firstService = new CheckoutService(firstCart);
        String foreignToken = firstService.beginCheckout();
        Cart secondCart = sampleCart();
        CheckoutService secondService = new CheckoutService(secondCart);
        assertThrows(IllegalArgumentException.class,
                () -> secondService.placeDemoOrder(foreignToken, customer()));
        assertEquals(3085L, secondCart.calculateTotalToea());
        assertEquals(3085L, firstCart.calculateTotalToea());
    }

    private Cart sampleCart() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        return cart;
    }

    private Customer customer() {
        return new Customer("Demo Shopper", "71234567", "Lae, Morobe Province");
    }

    private Product chicken() {
        return new GroceryProduct("chicken", "Zenag Kaikai", "Meat & Poultry", "900 g tray",
                1195, "Historical demo chicken tray", true);
    }

    private Product oil() {
        return new GroceryProduct("oil", "Healthy Choice cooking oil", "Groceries", "1 L bottle",
                695, "Historical demo cooking oil", false);
    }
}
