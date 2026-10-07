package com.desmondbeibi.stopnshop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Owns and validates cart changes; screens must obtain totals from this class. */
public final class Cart {
    private final ArrayList<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        CartItem candidate = new CartItem(product, quantity);
        int index = findIndex(product.getId());
        if (index >= 0) {
            CartItem existing = items.get(index);
            if (!sameProductDefinition(existing.getProduct(), product)) {
                throw new IllegalArgumentException("Product ID has conflicting catalogue data.");
            }
            candidate = new CartItem(existing.getProduct(), existing.getQuantity() + quantity);
        }
        validateTotalWithReplacement(index, candidate);
        if (index >= 0) {
            items.set(index, candidate);
        } else {
            items.add(candidate);
        }
    }

    public void setQuantity(String productId, int quantity) {
        int index = findIndex(requireId(productId));
        if (index < 0) {
            throw new IllegalArgumentException("Product is not in the cart.");
        }
        CartItem replacement = new CartItem(items.get(index).getProduct(), quantity);
        validateTotalWithReplacement(index, replacement);
        items.set(index, replacement);
    }

    /** Returns false when the valid ID is already absent. */
    public boolean removeProduct(String productId) {
        int index = findIndex(requireId(productId));
        if (index < 0) {
            return false;
        }
        items.remove(index);
        return true;
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Number of distinct product lines, rather than the total number of units. */
    public int getItemCount() {
        return items.size();
    }

    /** Number of units for the future cart badge. */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total = Math.addExact(total, item.getQuantity());
        }
        return total;
    }

    public long calculateTotalToea() {
        long total = 0;
        for (CartItem item : items) {
            total = Math.addExact(total, item.getSubtotalToea());
        }
        return total;
    }

    /** A read-only snapshot: callers cannot edit the cart or its line quantities. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    private int findIndex(String productId) {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).getProduct().getId().equals(productId)) {
                return index;
            }
        }
        return -1;
    }

    private static String requireId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        return productId.trim();
    }

    // Check before editing items, so a failed operation leaves the cart unchanged.
    private void validateTotalWithReplacement(int replacedIndex, CartItem replacement) {
        long total = replacement.getSubtotalToea();
        for (int index = 0; index < items.size(); index++) {
            if (index != replacedIndex) {
                total = Math.addExact(total, items.get(index).getSubtotalToea());
            }
        }
    }

    private static boolean sameProductDefinition(Product first, Product second) {
        return first.getClass().equals(second.getClass())
                && first.getName().equals(second.getName())
                && first.getCategory().equals(second.getCategory())
                && first.getUnitLabel().equals(second.getUnitLabel())
                && first.getDescription().equals(second.getDescription())
                && first.getUnitPriceToea() == second.getUnitPriceToea()
                && first.getHandlingInfo().equals(second.getHandlingInfo());
    }
}
