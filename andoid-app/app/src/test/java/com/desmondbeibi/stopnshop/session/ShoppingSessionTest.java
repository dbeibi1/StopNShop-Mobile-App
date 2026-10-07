package com.desmondbeibi.stopnshop.session;

import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.Order;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShoppingSessionTest {
    @Test
    public void separateCallersObserveTheSameCartChanges() {
        ShoppingSession session = new ShoppingSession();
        Cart listScreenCart = session.getCart();
        Cart cartScreenCart = session.getCart();
        listScreenCart.addProduct(session.getProductRepository().findById("P002"), 2);
        assertEquals(2390L, cartScreenCart.calculateTotalToea());
        cartScreenCart.setQuantity("P002", 3);
        assertEquals(3585L, listScreenCart.calculateTotalToea());
    }

    @Test
    public void checkoutKeepsTokenAndImmutableConfirmationAfterCartRefill() {
        ShoppingSession session = sampleSession();
        String token = session.beginCheckout();
        assertEquals(token, session.getCheckoutToken());
        assertEquals(token, session.beginCheckout());
        Order order = session.placeDemoOrder(token, customer());
        assertSame(order, session.getLastOrder());
        assertEquals(3085L, order.getTotalToea());
        assertTrue(session.getCart().isEmpty());
        assertEquals(token, session.getCheckoutToken());
        session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
        assertSame(order, session.placeDemoOrder(token, customer()));
        assertEquals(695L, session.getCart().calculateTotalToea());
        assertEquals(3085L, session.getLastOrder().getTotalToea());
        assertEquals(5, session.getProductRepository().getProducts().size());
    }

    @Test
    public void olderCompletedRetryCannotReplaceNewerConfirmation() {
        ShoppingSession session = sampleSession();
        String firstToken = session.beginCheckout();
        Order first = session.placeDemoOrder(firstToken, customer());
        session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
        String secondToken = session.beginCheckout();
        Order second = session.placeDemoOrder(secondToken, customer());
        session.getCart().addProduct(session.getProductRepository().findById("P004"), 2);
        assertSame(first, session.placeDemoOrder(firstToken, customer()));
        assertSame(second, session.getLastOrder());
        assertEquals(secondToken, session.getCheckoutToken());
        assertEquals(1390L, session.getCart().calculateTotalToea());
    }

    @Test
    public void invalidSubmissionPreservesReviewAndCartWithoutCreatingConfirmation() {
        ShoppingSession session = sampleSession();
        String token = session.beginCheckout();
        assertThrows(NullPointerException.class, () -> session.placeDemoOrder(token, null));
        assertThrows(IllegalArgumentException.class, () -> session.placeDemoOrder("unknown", customer()));
        assertNull(session.getLastOrder());
        assertEquals(token, session.getCheckoutToken());
        assertEquals(3085L, session.getCart().calculateTotalToea());
        assertEquals(3085L, session.placeDemoOrder(token, customer()).getTotalToea());
    }

    @Test
    public void staleReviewFailureKeepsEarlierConfirmationUntilNewCheckoutSucceeds() {
        ShoppingSession session = sampleSession();
        Order earlier = session.placeDemoOrder(session.beginCheckout(), customer());
        session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
        String staleToken = session.beginCheckout();
        session.getCart().setQuantity("P004", 2);
        assertThrows(IllegalStateException.class, () -> session.placeDemoOrder(staleToken, customer()));
        assertSame(earlier, session.getLastOrder());
        assertEquals(1390L, session.getCart().calculateTotalToea());
        String freshToken = session.beginCheckout();
        assertNotEquals(staleToken, freshToken);
        Order latest = session.placeDemoOrder(freshToken, customer());
        assertSame(latest, session.getLastOrder());
        assertEquals(1390L, latest.getTotalToea());
    }

    @Test
    public void newSessionStartsFreshAndCannotUseAnotherSessionsToken() {
        ShoppingSession previous = sampleSession();
        String token = previous.beginCheckout();
        previous.placeDemoOrder(token, customer());
        previous.getCart().addProduct(previous.getProductRepository().findById("P004"), 1);
        ShoppingSession fresh = new ShoppingSession();
        assertTrue(fresh.getCart().isEmpty());
        assertNull(fresh.getCheckoutToken());
        assertNull(fresh.getLastOrder());
        assertEquals(5, fresh.getProductRepository().getProducts().size());
        assertThrows(IllegalArgumentException.class, () -> fresh.placeDemoOrder(token, customer()));
        assertEquals(695L, previous.getCart().calculateTotalToea());
    }

    @Test
    public void emptySessionCannotBeginReviewOrCreateCheckoutState() {
        ShoppingSession session = new ShoppingSession();
        assertThrows(IllegalStateException.class, session::beginCheckout);
        assertNull(session.getCheckoutToken());
        assertNull(session.getLastOrder());
    }

    @Test
    public void trimmedSubmissionTokenStillTracksTheSuccessfulConfirmation() {
        ShoppingSession session = sampleSession();
        String token = session.beginCheckout();
        Order order = session.placeDemoOrder(" " + token + " ", customer());
        assertSame(order, session.getLastOrder());
        assertTrue(session.getCart().isEmpty());
    }


    @Test public void reviewedLinesRemainFrozenUntilAnExplicitNewReview() {
        ShoppingSession session = sampleSession();
        session.beginCheckout();
        java.util.List<com.desmondbeibi.stopnshop.model.OrderLine> first = session.getCheckoutLines();
        assertEquals(2, first.get(0).getQuantity());
        assertThrows(UnsupportedOperationException.class, first::clear);
        session.getCart().setQuantity("P002", 3);
        assertEquals(2, session.getCheckoutLines().get(0).getQuantity());
        session.beginCheckout();
        assertEquals(3, session.getCheckoutLines().get(0).getQuantity());
        assertEquals(2, first.get(0).getQuantity());
    }

    @Test public void reviewSurvivesSuccessWhileNewSessionHasNoReview() {
        ShoppingSession session = sampleSession();
        String token = session.beginCheckout();
        session.placeDemoOrder(token, customer());
        assertTrue(session.getCart().isEmpty());
        assertEquals(2, session.getCheckoutLines().size());
        assertEquals(2390L, session.getCheckoutLines().get(0).getSubtotalToea());
        assertTrue(new ShoppingSession().getCheckoutLines().isEmpty());
    }

    private ShoppingSession sampleSession() {
        ShoppingSession session = new ShoppingSession();
        session.getCart().addProduct(session.getProductRepository().findById("P002"), 2);
        session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
        return session;
    }

    private Customer customer() {
        return new Customer("Demo Shopper", "71234567", "Lae, Morobe Province");
    }
}
