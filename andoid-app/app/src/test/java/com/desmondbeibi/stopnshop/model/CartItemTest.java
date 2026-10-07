package com.desmondbeibi.stopnshop.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class CartItemTest {
    @Test
    public void subtotalUsesExactNonRoundPriceAndQuantity() {
        CartItem item = new CartItem(product(1195), 2);
        assertEquals(2390L, item.getSubtotalToea());
    }

    @Test
    public void quantitiesOutsideOneToNinetyNineAreRejected() {
        Product product = product(695);
        for (int quantity : new int[]{Integer.MIN_VALUE, -1, 0, 100, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new CartItem(product, quantity));
        }
        assertEquals(695L, new CartItem(product, 1).getSubtotalToea());
        assertEquals(68805L, new CartItem(product, 99).getSubtotalToea());
    }

    @Test
    public void nullProductIsRejected() {
        assertThrows(NullPointerException.class, () -> new CartItem(null, 1));
    }

    @Test
    public void unrepresentableSubtotalIsRejectedRatherThanWrapping() {
        assertThrows(ArithmeticException.class, () -> new CartItem(product(Long.MAX_VALUE), 2));
    }

    private Product product(long price) {
        return new GroceryProduct("test", "Test product", "Groceries", "1 pack",
                price, "Unit test data", false);
    }
}
