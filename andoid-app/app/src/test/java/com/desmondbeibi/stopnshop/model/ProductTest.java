package com.desmondbeibi.stopnshop.model;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ProductTest {
    @Test
    public void mixedProductsUseTheirOwnHandlingInformation() {
        ArrayList<Product> products = new ArrayList<>();
        products.add(new GroceryProduct("chicken", "Zenag Kaikai", "Meat & Poultry",
                "900 g tray", 1195, "Historical demo chicken tray", true));
        products.add(new HouseholdProduct("demo-soap", "Demo laundry soap", "Household",
                "1 bar", 500, "Synthetic test item; not from the supplied flyer"));

        ArrayList<String> handling = new ArrayList<>();
        for (Product product : products) {
            handling.add(product.getHandlingInfo());
        }
        assertTrue(handling.get(0).contains("Keep chilled"));
        assertTrue(handling.get(1).contains("use and safety"));
        assertNotEquals(handling.get(0), handling.get(1));
    }

    @Test
    public void nonPerishableGroceryUsesDryStorageInformation() {
        Product oil = new GroceryProduct("oil", "Oil", "Groceries", "1 L bottle",
                695, "Historical demo cooking oil", false);
        assertTrue(oil.getHandlingInfo().contains("cool, dry place"));
        assertFalse(oil.getHandlingInfo().contains("Keep chilled"));
    }

    @Test
    public void catalogueTextIsTrimmedAtConstruction() {
        Product product = new GroceryProduct(" oil ", " Oil ", " Groceries ",
                " 1 L bottle ", 695, " Demo oil ", false);
        assertEquals("oil", product.getId());
        assertEquals("Oil", product.getName());
        assertEquals("Groceries", product.getCategory());
        assertEquals("1 L bottle", product.getUnitLabel());
        assertEquals("Demo oil", product.getDescription());
    }

    @Test
    public void missingCatalogueFieldsAndNegativePricesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> grocery(null, "Oil", "Grocery", "1 L", 695, "Demo"));
        assertThrows(IllegalArgumentException.class, () -> grocery(" ", "Oil", "Grocery", "1 L", 695, "Demo"));
        assertThrows(IllegalArgumentException.class, () -> grocery("oil", " ", "Grocery", "1 L", 695, "Demo"));
        assertThrows(IllegalArgumentException.class, () -> grocery("oil", "Oil", " ", "1 L", 695, "Demo"));
        assertThrows(IllegalArgumentException.class, () -> grocery("oil", "Oil", "Grocery", " ", 695, "Demo"));
        assertThrows(IllegalArgumentException.class, () -> grocery("oil", "Oil", "Grocery", "1 L", 695, " "));
        assertThrows(IllegalArgumentException.class, () -> grocery("oil", "Oil", "Grocery", "1 L", -1, "Demo"));
    }

    @Test
    public void zeroPriceIsAllowedByTheNonNegativePriceRule() {
        Cart cart = new Cart();
        cart.addProduct(grocery("sample", "Sample", "Grocery", "1 pack", 0, "Free demo sample"), 2);
        assertEquals(0L, cart.calculateTotalToea());
        assertEquals(2, cart.getTotalQuantity());
        assertFalse(cart.isEmpty());
    }

    private Product grocery(String id, String name, String category, String unit,
                            long price, String description) {
        return new GroceryProduct(id, name, category, unit, price, description, false);
    }
}
