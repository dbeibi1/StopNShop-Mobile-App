package com.desmondbeibi.stopnshop.model;

/** A food product with storage information determined by its perishability. */
public final class GroceryProduct extends Product {
    private final boolean perishable;

    public GroceryProduct(String id, String name, String category, String unitLabel,
                          long unitPriceToea, String description, boolean perishable) {
        super(id, name, category, unitLabel, unitPriceToea, description);
        this.perishable = perishable;
    }

    public boolean isPerishable() {
        return perishable;
    }

    @Override
    public String getHandlingInfo() {
        if (perishable) {
            return "Keep chilled. Follow the pack's storage instructions.";
        }
        return "Store in a cool, dry place. Follow the pack's storage instructions.";
    }
}
