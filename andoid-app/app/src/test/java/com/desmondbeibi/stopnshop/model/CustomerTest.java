package com.desmondbeibi.stopnshop.model;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CustomerTest {
    @Test
    public void nonBreakingAndUnicodeWhitespaceCannotBypassRequiredFields() {
        Customer.ValidationException error = assertThrows(Customer.ValidationException.class,
                () -> new Customer("\u00a0\u2003", "\u00a0", "\u2009\u00a0"));
        assertEquals(3, error.getErrors().size());
        Customer customer = new Customer("\u00a0Demo Shopper\u2003", "71234567", "\u2009Lae\u00a0");
        assertEquals("Demo Shopper", customer.getName());
        assertEquals("Lae", customer.getLocation());
    }
    @Test
    public void validDetailsAreTrimmedAndPhoneSeparatorsAreRemoved() {
        Customer customer = new Customer(" Demo Shopper ", " +675 7123-4567 ", " Lae, Morobe Province ");
        assertEquals("Demo Shopper", customer.getName());
        assertEquals("+67571234567", customer.getPhone());
        assertEquals("Lae, Morobe Province", customer.getLocation());
        assertTrue(Customer.validate(customer.getName(), customer.getPhone(), customer.getLocation()).isEmpty());
    }

    @Test
    public void allowedLengthBoundariesAreAcceptedWithoutTruncating() {
        String name = "N".repeat(Customer.MAX_NAME_LENGTH);
        String location = "L".repeat(Customer.MAX_LOCATION_LENGTH);
        Customer shortest = new Customer(name, "1234567", location);
        Customer longest = new Customer(name, "+123456789012345", location);
        assertEquals(name, shortest.getName());
        assertEquals(location, longest.getLocation());
        assertEquals("+123456789012345", longest.getPhone());
    }

    @Test
    public void blankInputReturnsAllFieldErrorsAndCannotCreateCustomer() {
        Customer.ValidationException error = assertThrows(Customer.ValidationException.class,
                () -> new Customer(" \t ", " ", "\n"));
        Map<Customer.Field, String> fields = error.getErrors();
        assertEquals(3, fields.size());
        assertTrue(fields.get(Customer.Field.NAME).contains("required"));
        assertTrue(fields.get(Customer.Field.PHONE).contains("required"));
        assertTrue(fields.get(Customer.Field.LOCATION).contains("required"));
        assertThrows(UnsupportedOperationException.class, fields::clear);
    }

    @Test
    public void nullInputsProduceFieldErrorsInsteadOfAnUnexpectedNullCrash() {
        Map<Customer.Field, String> errors = Customer.validate(null, null, null);
        assertEquals(3, errors.size());
        assertThrows(Customer.ValidationException.class, () -> new Customer(null, null, null));
        assertThrows(UnsupportedOperationException.class, errors::clear);
    }

    @Test
    public void unsupportedPhoneCharactersAndMisplacedPlusAreRejected() {
        for (String phone : new String[]{
                "7123ABCD", "675+71234567", "++67571234567", "(675)71234567",
                "7123/4567", "7123\t4567", "٧١٢٣٤٥٦٧"}) {
            Customer.ValidationException error = assertThrows(Customer.ValidationException.class,
                    () -> new Customer("Demo Shopper", phone, "Lae"));
            assertEquals(1, error.getErrors().size());
            assertTrue(error.getErrors().containsKey(Customer.Field.PHONE));
        }
    }

    @Test
    public void phoneDigitCountIsCheckedAfterRemovingAllowedSeparators() {
        for (String phone : new String[]{"12 34-56", "+1234 5678 9012 3456"}) {
            assertThrows(Customer.ValidationException.class,
                    () -> new Customer("Demo Shopper", phone, "Lae"));
        }
        assertEquals("71234567", new Customer("Demo Shopper", "7123-4567", "Lae").getPhone());
    }

    @Test
    public void excessiveNameAndLocationReturnSpecificErrorsWithoutTruncating() {
        Map<Customer.Field, String> errors = Customer.validate("N".repeat(101), "71234567", "L".repeat(201));
        assertEquals(2, errors.size());
        assertTrue(errors.get(Customer.Field.NAME).contains("100"));
        assertTrue(errors.get(Customer.Field.LOCATION).contains("200"));
        assertFalse(errors.containsKey(Customer.Field.PHONE));
        assertThrows(Customer.ValidationException.class,
                () -> new Customer("N".repeat(101), "71234567", "L".repeat(201)));
    }
}
