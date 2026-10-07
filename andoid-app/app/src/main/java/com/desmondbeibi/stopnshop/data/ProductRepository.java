package com.desmondbeibi.stopnshop.data;

import com.desmondbeibi.stopnshop.model.GroceryProduct;
import com.desmondbeibi.stopnshop.model.HouseholdProduct;
import com.desmondbeibi.stopnshop.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The local academic-demo catalogue; no network or current retailer stock is queried. */
public final class ProductRepository {
    public static final String MEAT_AND_POULTRY = "Meat & Poultry";
    public static final String FRESH_PRODUCE = "Fresh Produce";
    public static final String GROCERY = "Grocery";
    public static final String HOUSEHOLD = "Household";

    private final ArrayList<Product> products = new ArrayList<>();

    public ProductRepository() {
        products.add(new GroceryProduct("P001", "Lamb Neck Chops", MEAT_AND_POULTRY,
                "Demo 1 kg pack", 2195,
                "Historical flyer sample from August 2018. The 1 kg pack is an app buying-unit adaptation.",
                true));
        products.add(new GroceryProduct("P002", "Zenag Kaikai", MEAT_AND_POULTRY,
                "900 g tray", 1195, "Chicken tray using the historical August 2018 flyer price.", true));
        products.add(new GroceryProduct("P003", "English Green Cabbage", FRESH_PRODUCE,
                "Demo 1 kg pack", 495,
                "Historical flyer sample from August 2018. The 1 kg pack is an app buying-unit adaptation.",
                true));
        products.add(new GroceryProduct("P004", "Healthy Choice Cooking Oil", GROCERY,
                "1 L bottle", 695, "Cooking oil using the historical August 2018 flyer price.", false));
        products.add(new HouseholdProduct("P005", "Demo Laundry Soap", HOUSEHOLD,
                "1 bar", 500,
                "Synthetic household example. K5.00 is a chosen demo price, not a retailer price."));
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(new ArrayList<>(products));
    }

    /** Categories follow catalogue order and are always backed by at least one product. */
    public List<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();
        for (Product product : products) {
            if (!categories.contains(product.getCategory())) {
                categories.add(product.getCategory());
            }
        }
        return Collections.unmodifiableList(categories);
    }

    /** Returns null for an unknown valid ID; a details screen must handle that result. */
    public Product findById(String id) {
        String wantedId = requireText(id, "Product ID");
        for (Product product : products) {
            if (product.getId().equals(wantedId)) {
                return product;
            }
        }
        return null;
    }

    /** Unknown valid categories return an empty list; matching is case-sensitive. */
    public List<Product> findByCategory(String category) {
        String wantedCategory = requireText(category, "Category");
        ArrayList<Product> matches = new ArrayList<>();
        for (Product product : products) {
            if (product.getCategory().equals(wantedCategory)) {
                matches.add(product);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
