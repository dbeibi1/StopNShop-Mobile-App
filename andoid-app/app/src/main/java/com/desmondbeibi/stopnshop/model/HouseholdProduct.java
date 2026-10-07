package com.desmondbeibi.stopnshop.model;

/** A household item uses shared pricing information but supplies non-food handling advice. */
public final class HouseholdProduct extends Product {
    public HouseholdProduct(String id, String name, String category, String unitLabel,
                            long unitPriceToea, String description) {
        super(id, name, category, unitLabel, unitPriceToea, description);
    }

    @Override
    public String getHandlingInfo() {
        return "Keep away from food. Follow the label's use and safety instructions.";
    }
}
