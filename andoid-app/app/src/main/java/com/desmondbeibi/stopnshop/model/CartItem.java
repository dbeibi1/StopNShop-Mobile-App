package com.desmondbeibi.stopnshop.model;

import java.util.Objects;

/** An immutable cart line. Quantity changes create a replacement through Cart. */
public final class CartItem {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 99;

    private final Product product;
    private final int quantity;

    public CartItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "Product is required.");
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
        // Reject an unrepresentable subtotal before this line becomes part of a cart.
        Math.multiplyExact(product.getUnitPriceToea(), (long) quantity);
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotalToea() {
        return Math.multiplyExact(product.getUnitPriceToea(), (long) quantity);
    }
}
