package com.desmondbeibi.stopnshop.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderTest {
    @Test
    public void orderKeepsPurchasedValuesAfterCartAndCatalogueReplacement() {
        Cart cart = new Cart();
        cart.addProduct(product("chicken", "Zenag Kaikai", "900 g tray", 1195), 2);
        cart.addProduct(product("oil", "Healthy Choice cooking oil", "1 L bottle", 695), 1);
        ArrayList<OrderLine> snapshot = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            snapshot.add(new OrderLine(item));
        }
        Order order = new Order(" DEMO-test ", customer(), snapshot);
        cart.setQuantity("chicken", 1);
        cart.clear();
        cart.addProduct(product("chicken", "New catalogue name", "New pack", 9999), 1);
        snapshot.clear();

        assertEquals("DEMO-test", order.getReference());
        assertEquals(3085L, order.getTotalToea());
        assertEquals(3, order.getTotalQuantity());
        assertEquals("Zenag Kaikai", order.getLines().get(0).getName());
        assertEquals("900 g tray", order.getLines().get(0).getUnitLabel());
        assertEquals(1195L, order.getLines().get(0).getUnitPriceToea());
        assertEquals(2, order.getLines().get(0).getQuantity());
        assertEquals(2390L, order.getLines().get(0).getSubtotalToea());
        assertEquals("Lae, Morobe Province", order.getCustomer().getLocation());
    }

    @Test
    public void callerCannotEditOrderLinesOrAppendThroughTheOriginalList() {
        List<OrderLine> original = new ArrayList<>();
        original.add(line("oil", 695, 1));
        Order order = new Order("DEMO-test", customer(), original);
        original.add(line("chicken", 1195, 2));
        assertEquals(1, order.getLines().size());
        assertEquals(695L, order.getTotalToea());
        assertThrows(UnsupportedOperationException.class, () -> order.getLines().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> order.getLines().set(0, line("replacement", 100, 1)));
    }

    @Test
    public void missingReferenceCustomerOrLineListCannotCreateOrder() {
        List<OrderLine> valid = Collections.singletonList(line("oil", 695, 1));
        assertThrows(IllegalArgumentException.class, () -> new Order(null, customer(), valid));
        assertThrows(IllegalArgumentException.class, () -> new Order(" ", customer(), valid));
        assertThrows(NullPointerException.class, () -> new Order("DEMO-test", null, valid));
        assertThrows(NullPointerException.class, () -> new Order("DEMO-test", customer(), null));
    }

    @Test
    public void emptyOrNullLinesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("DEMO-test", customer(), Collections.emptyList()));
        assertThrows(NullPointerException.class,
                () -> new Order("DEMO-test", customer(), Arrays.asList(line("oil", 695, 1), null)));
        assertThrows(NullPointerException.class, () -> new OrderLine(null));
    }

    @Test
    public void duplicateProductIdsCannotCreateAmbiguousOrderLines() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("DEMO-test", customer(), Arrays.asList(line("oil", 695, 1), line("oil", 695, 2))));
    }

    @Test
    public void overflowingOrderTotalIsRejectedInsteadOfWrapping() {
        assertThrows(ArithmeticException.class,
                () -> new Order("DEMO-test", customer(),
                        Arrays.asList(line("large", Long.MAX_VALUE, 1), line("extra", 1, 1))));
    }

    @Test
    public void zeroCostNonEmptyOrderRetainsItsUnits() {
        Order order = new Order("DEMO-test", customer(), Collections.singletonList(line("sample", 0, 2)));
        assertEquals(0L, order.getTotalToea());
        assertEquals(2, order.getTotalQuantity());
        assertEquals(1, order.getLines().size());
    }

    private Customer customer() {
        return new Customer("Demo Shopper", "71234567", "Lae, Morobe Province");
    }

    private OrderLine line(String id, long price, int quantity) {
        return new OrderLine(new CartItem(product(id, id, "1 pack", price), quantity));
    }

    private Product product(String id, String name, String unit, long price) {
        return new GroceryProduct(id, name, "Groceries", unit, price, "Unit test data", false);
    }
}
