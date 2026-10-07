package com.desmondbeibi.stopnshop.model;

import java.util.Objects;

/** Copies the purchased values, rather than retaining a live cart or Product reference. */
public final class OrderLine {
    private final String productId;
    private final String name;
    private final String unitLabel;
    private final long unitPriceToea;
    private final int quantity;

    public OrderLine(CartItem item) {
        Objects.requireNonNull(item, "Cart item is required.");
        Product product = item.getProduct();
        productId = product.getId();
        name = product.getName();
        unitLabel = product.getUnitLabel();
        unitPriceToea = product.getUnitPriceToea();
        quantity = item.getQuantity();
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public long getUnitPriceToea() {
        return unitPriceToea;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotalToea() {
        return Math.multiplyExact(unitPriceToea, (long) quantity);
    }

    /** Value equality lets checkout detect changes to the reviewed items or prices. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderLine)) {
            return false;
        }
        OrderLine line = (OrderLine) other;
        return unitPriceToea == line.unitPriceToea
                && quantity == line.quantity
                && productId.equals(line.productId)
                && name.equals(line.name)
                && unitLabel.equals(line.unitLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, unitLabel, unitPriceToea, quantity);
    }
}
