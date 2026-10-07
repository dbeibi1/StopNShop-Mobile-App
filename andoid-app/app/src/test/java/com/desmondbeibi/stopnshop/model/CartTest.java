package com.desmondbeibi.stopnshop.model;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CartTest {
    @Test
    public void emptyCartHasNoLinesUnitsOrCost() {
        Cart cart = new Cart();
        assertTrue(cart.isEmpty());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(0, cart.getTotalQuantity());
        assertEquals(0L, cart.calculateTotalToea());
    }

    @Test
    public void twoChickenTraysAndOilCostThirtyKinaEightyFiveToea() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        assertEquals(2390L, cart.getItems().get(0).getSubtotalToea());
        assertEquals(695L, cart.getItems().get(1).getSubtotalToea());
        assertEquals(3085L, cart.calculateTotalToea());
        assertEquals(3, cart.getTotalQuantity());
        assertEquals(2, cart.getItemCount());
    }

    @Test
    public void lambAndCabbageCostTwentySixKinaNinetyToea() {
        Cart cart = new Cart();
        cart.addProduct(product("lamb", "Lamb neck chops", 2195), 1);
        cart.addProduct(product("cabbage", "English green cabbage", 495), 1);
        assertEquals(2690L, cart.calculateTotalToea());
    }

    @Test
    public void separateObjectsWithTheSameIdMergeIntoOneLine() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 1);
        cart.addProduct(chicken(), 2);
        assertEquals(1, cart.getItemCount());
        assertEquals(3, cart.getTotalQuantity());
        assertEquals(3585L, cart.calculateTotalToea());
    }

    @Test
    public void identicalNamesWithDifferentIdsRemainSeparate() {
        Cart cart = new Cart();
        cart.addProduct(product("pack-a", "Test pack", 100), 1);
        cart.addProduct(product("pack-b", "Test pack", 200), 1);
        assertEquals(2, cart.getItemCount());
        assertEquals(300L, cart.calculateTotalToea());
    }

    @Test
    public void quantityChangeRecalculatesLineTotalAndBadge() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        cart.setQuantity("chicken", 1);
        assertEquals(1890L, cart.calculateTotalToea());
        assertEquals(2, cart.getTotalQuantity());
        cart.setQuantity(" oil ", 3);
        assertEquals(3280L, cart.calculateTotalToea());
        assertEquals(4, cart.getTotalQuantity());
    }

    @Test
    public void removalRecalculatesTotalsAndLastRemovalEmptiesCart() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        assertTrue(cart.removeProduct("chicken"));
        assertEquals(695L, cart.calculateTotalToea());
        assertEquals(1, cart.getTotalQuantity());
        assertFalse(cart.removeProduct("chicken"));
        assertTrue(cart.removeProduct("oil"));
        assertTrue(cart.isEmpty());
        assertEquals(0L, cart.calculateTotalToea());
    }

    @Test
    public void clearResetsCartAndAllowsShoppingAgain() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        cart.addProduct(oil(), 1);
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalQuantity());
        assertEquals(0L, cart.calculateTotalToea());
        cart.addProduct(oil(), 1);
        assertEquals(695L, cart.calculateTotalToea());
    }

    @Test
    public void returnedItemsCannotEditCartAndRemainAStableSnapshot() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        List<CartItem> snapshot = cart.getItems();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, new CartItem(oil(), 1)));
        cart.setQuantity("chicken", 3);
        cart.clear();
        assertEquals(2, snapshot.get(0).getQuantity());
        assertEquals(2390L, snapshot.get(0).getSubtotalToea());
        assertTrue(cart.isEmpty());
    }

    @Test
    public void invalidAddsAndUpdatesLeaveExistingCartUnchanged() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        for (int quantity : new int[]{-1, 0, 100, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> cart.addProduct(oil(), quantity));
            assertThrows(IllegalArgumentException.class, () -> cart.setQuantity("chicken", quantity));
        }
        assertThrows(NullPointerException.class, () -> cart.addProduct(null, 1));
        assertThrows(IllegalArgumentException.class, () -> cart.setQuantity("missing", 1));
        assertThrows(IllegalArgumentException.class, () -> cart.setQuantity(null, 1));
        assertThrows(IllegalArgumentException.class, () -> cart.removeProduct(" "));
        assertEquals(1, cart.getItemCount());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(2390L, cart.calculateTotalToea());
    }

    @Test
    public void ninetyNineUnitLimitRejectsFurtherAddWithoutLosingUnits() {
        Cart cart = new Cart();
        cart.addProduct(oil(), 98);
        cart.addProduct(oil(), 1);
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(oil(), 1));
        assertEquals(99, cart.getTotalQuantity());
        assertEquals(68805L, cart.calculateTotalToea());
    }

    @Test
    public void conflictingCatalogueDataForSameIdCannotChangeCart() {
        Cart cart = new Cart();
        cart.addProduct(chicken(), 2);
        Product changedPrice = new GroceryProduct("chicken", "Zenag Kaikai", "Meat & Poultry",
                "900 g tray", 1295, "Historical demo chicken tray", true);
        Product changedStorage = new GroceryProduct("chicken", "Zenag Kaikai", "Meat & Poultry",
                "900 g tray", 1195, "Historical demo chicken tray", false);
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(changedPrice, 1));
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(changedStorage, 1));
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(2390L, cart.calculateTotalToea());
    }

    @Test
    public void overflowingTotalOnAddDoesNotInsertAnotherLine() {
        Cart cart = new Cart();
        cart.addProduct(product("large", "Large test price", Long.MAX_VALUE), 1);
        assertThrows(ArithmeticException.class, () -> cart.addProduct(product("extra", "Extra", 1), 1));
        assertEquals(1, cart.getItemCount());
        assertEquals(Long.MAX_VALUE, cart.calculateTotalToea());
    }

    @Test
    public void overflowingTotalOnUpdatePreservesPreviousQuantity() {
        Cart cart = new Cart();
        cart.addProduct(product("large", "Large test price", Long.MAX_VALUE / 2), 1);
        cart.addProduct(product("extra", "Extra", 2), 1);
        assertThrows(ArithmeticException.class, () -> cart.setQuantity("large", 2));
        assertEquals(1, cart.getItems().get(0).getQuantity());
        assertEquals(Long.MAX_VALUE / 2 + 2, cart.calculateTotalToea());
    }

    private Product chicken() {
        return new GroceryProduct("chicken", "Zenag Kaikai", "Meat & Poultry",
                "900 g tray", 1195, "Historical demo chicken tray", true);
    }

    private Product oil() {
        return new GroceryProduct("oil", "Healthy Choice cooking oil", "Groceries",
                "1 L bottle", 695, "Historical demo cooking oil", false);
    }

    private Product product(String id, String name, long price) {
        return new GroceryProduct(id, name, "Groceries", "1 kg demo pack",
                price, "Unit test data", false);
    }
}
