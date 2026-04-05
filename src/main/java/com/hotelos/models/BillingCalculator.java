package com.hotelos.models;

/**
 * 5. Generics
 * Use bounded type parameters (<T extends Number>) for a billing calculator.
 */
public class BillingCalculator<T extends Number> {

    // Demonstrates Wrapper classes (Double) and Box/Unbox (doubleValue)
    // Calculates a discounted total for a specific room billing.
    public Double calculateTotalWithDiscount(T baseTariff, T discountPercentage) {
        double tariff = baseTariff.doubleValue();
        double discount = discountPercentage.doubleValue();

        double finalAmount = tariff - (tariff * (discount / 100));
        return finalAmount;
    }
}
