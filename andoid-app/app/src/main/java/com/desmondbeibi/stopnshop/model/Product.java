package com.desmondbeibi.stopnshop.model;

/** Shared, immutable catalogue information. Prices are integer toea, not decimal kina. */
public abstract class Product {
    private final String id;
    private final String name;
    private final String category;
    private final String unitLabel;
    private final String description;
    private final long unitPriceToea;

    protected Product(String id, String name, String category, String unitLabel,
                      long unitPriceToea, String description) {
        this.id = requireText(id, "Product ID");
        this.name = requireText(name, "Product name");
        this.category = requireText(category, "Category");
        this.unitLabel = requireText(unitLabel, "Unit label");
        this.description = requireText(description, "Description");
        if (unitPriceToea < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.unitPriceToea = unitPriceToea;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getCategory() {
        return category;
    }

    public final String getUnitLabel() {
        return unitLabel;
    }

    public final String getDescription() {
        return description;
    }

    public final long getUnitPriceToea() {
        return unitPriceToea;
    }

    /** Subclasses supply the handling information shown on a future details screen. */
    public abstract String getHandlingInfo();
}
