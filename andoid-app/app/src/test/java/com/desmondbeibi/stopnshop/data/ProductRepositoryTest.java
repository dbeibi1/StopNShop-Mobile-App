package com.desmondbeibi.stopnshop.data;

import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.GroceryProduct;
import com.desmondbeibi.stopnshop.model.HouseholdProduct;
import com.desmondbeibi.stopnshop.model.Product;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ProductRepositoryTest {
    @Test
    public void flyerCatalogueBuildsBothKnownExactPriceCarts() {
        ProductRepository repository = new ProductRepository();
        Cart cart = new Cart();
        cart.addProduct(repository.findById("P002"), 2);
        cart.addProduct(repository.findById("P004"), 1);
        assertEquals(3085L, cart.calculateTotalToea());
        assertEquals("900 g tray", cart.getItems().get(0).getProduct().getUnitLabel());
        assertEquals("1 L bottle", cart.getItems().get(1).getProduct().getUnitLabel());
        cart.clear();
        cart.addProduct(repository.findById("P001"), 1);
        cart.addProduct(repository.findById("P003"), 1);
        assertEquals(2690L, cart.calculateTotalToea());
        assertEquals("Demo 1 kg pack", cart.getItems().get(0).getProduct().getUnitLabel());
        assertEquals("Demo 1 kg pack", cart.getItems().get(1).getProduct().getUnitLabel());
    }

    @Test
    public void everyCategoryReturnsOnlyItsOwnProductsAndCoversTheCatalogue() {
        ProductRepository repository = new ProductRepository();
        assertEquals(Arrays.asList("Meat & Poultry", "Fresh Produce", "Grocery", "Household"),
                repository.getCategories());
        int matched = 0;
        for (String category : repository.getCategories()) {
            List<Product> products = repository.findByCategory(category);
            assertFalse(products.isEmpty());
            for (Product product : products) {
                assertEquals(category, product.getCategory());
            }
            matched += products.size();
        }
        assertEquals(5, matched);
        assertEquals(2, repository.findByCategory("Meat & Poultry").size());
        assertEquals("P003", repository.findByCategory("Fresh Produce").get(0).getId());
        assertEquals("P004", repository.findByCategory("Grocery").get(0).getId());
        assertEquals("P005", repository.findByCategory("Household").get(0).getId());
    }

    @Test
    public void mixedCatalogueDispatchesHandlingAndLabelsSyntheticHouseholdData() {
        ProductRepository repository = new ProductRepository();
        ArrayList<Product> products = new ArrayList<>(repository.getProducts());
        Product grocery = products.get(1);
        Product household = products.get(4);
        assertTrue(grocery instanceof GroceryProduct);
        assertTrue(household instanceof HouseholdProduct);
        assertTrue(grocery.getHandlingInfo().contains("Keep chilled"));
        assertTrue(household.getHandlingInfo().contains("use and safety"));
        assertTrue(household.getName().startsWith("Demo"));
        assertTrue(household.getDescription().contains("Synthetic"));
        assertEquals(500L, household.getUnitPriceToea());
        for (int index = 0; index < 4; index++) {
            assertTrue(products.get(index).getDescription().contains("2018"));
        }
    }

    @Test
    public void returnedCollectionsCannotCorruptCatalogueOrCategories() {
        ProductRepository repository = new ProductRepository();
        List<Product> all = repository.getProducts();
        List<String> categories = repository.getCategories();
        List<Product> filtered = repository.findByCategory("Meat & Poultry");
        assertThrows(UnsupportedOperationException.class, all::clear);
        assertThrows(UnsupportedOperationException.class, categories::clear);
        assertThrows(UnsupportedOperationException.class, filtered::clear);
        assertThrows(UnsupportedOperationException.class, () -> all.set(0, all.get(1)));
        assertEquals(5, repository.getProducts().size());
        assertEquals(4, repository.getCategories().size());
    }

    @Test
    public void lookupsUseStableIdsAndHandleUnknownValues() {
        ProductRepository repository = new ProductRepository();
        assertSame(repository.getProducts().get(0), repository.findById(" P001 "));
        assertEquals(1, repository.findByCategory(" Grocery ").size());
        assertNull(repository.findById("P404"));
        assertTrue(repository.findByCategory("Unknown category").isEmpty());
        assertTrue(repository.findByCategory("grocery").isEmpty());
    }

    @Test
    public void missingLookupArgumentsAreRejected() {
        ProductRepository repository = new ProductRepository();
        for (String value : new String[]{null, "", " \t "}) {
            assertThrows(IllegalArgumentException.class, () -> repository.findById(value));
            assertThrows(IllegalArgumentException.class, () -> repository.findByCategory(value));
        }
    }

    @Test
    public void allCatalogueIdsAreUnique() {
        ProductRepository repository = new ProductRepository();
        Set<String> ids = new HashSet<>();
        for (Product product : repository.getProducts()) {
            assertTrue(ids.add(product.getId()));
        }
        assertEquals(5, ids.size());
    }
}
