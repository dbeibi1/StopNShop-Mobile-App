package com.desmondbeibi.stopnshop;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.session.ShoppingSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/** Native integration: inputs, frozen reviews, recreation, submission and confirmation. */
@RunWith(AndroidJUnit4.class)
public class CheckoutFlowTest {
    private ActivityScenario<MainActivity> scenario;
    @Before public void fresh() {
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(a -> state(a).getCart().clear());
        scenario.recreate();
    }
    @After public void clean() {
        if (scenario != null) {
            scenario.onActivity(a -> state(a).getCart().clear());
            scenario.close();
        }
    }
    private ShoppingSession state(MainActivity a) {
        return ((StopNShopApplication) a.getApplication()).getShoppingSession();
    }
    private Matcher<View> product(int id, String code) {
        return allOf(withId(id), isDescendantOfA(withTagValue(is((Object) code))));
    }
    private void category(String category) {
        onView(withId(R.id.nav_categories)).perform(click());
        onView(withTagValue(is((Object) category))).perform(scrollTo(), click());
    }
    private void seedBasket() {
        scenario.onActivity(a -> {
            ShoppingSession s = state(a);
            s.getCart().addProduct(s.getProductRepository().findById("P002"), 2);
            s.getCart().addProduct(s.getProductRepository().findById("P004"), 1);
        });
        onView(withId(R.id.nav_cart)).perform(click());
    }
    private void checkout() { onView(withId(R.id.cart_checkout)).perform(scrollTo(), click()); }
    private void fill(String name, String phone, String location) {
        onView(withId(R.id.checkout_name)).perform(scrollTo(), replaceText(name), closeSoftKeyboard());
        onView(withId(R.id.checkout_phone)).perform(scrollTo(), replaceText(phone), closeSoftKeyboard());
        onView(withId(R.id.checkout_location)).perform(scrollTo(), replaceText(location), closeSoftKeyboard());
    }
    private void submit() { onView(withId(R.id.checkout_submit)).perform(scrollTo(), click()); }
    private void top() {
        scenario.onActivity(a -> ((ScrollView) a.findViewById(R.id.screen_scroll)).fullScroll(View.FOCUS_UP));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void capture(String name) throws Exception {
        File dir = new File(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getExternalFilesDir(null), "visual-flow-screenshots");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create evidence folder");
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try (FileOutputStream output = new FileOutputStream(new File(dir,
                "2026-10-06_" + name + "_emulator_v06.png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        } finally { bitmap.recycle(); }
    }

    @Test public void detailsQuantityAndPolymorphicHandlingSurviveRecreation() throws Exception {
        capture("S02_home");
        category("Meat & Poultry");
        capture("S04_meat-products");
        onView(product(R.id.product_details, "P002")).perform(scrollTo(), click());
        onView(withId(R.id.details_minus)).check(matches(not(isEnabled())));
        onView(withId(R.id.details_plus)).perform(scrollTo(), click());
        scenario.recreate();
        onView(withId(R.id.details_quantity)).check(matches(withText("Quantity: 2")));
        onView(withId(R.id.details_subtotal)).check(matches(withText("Line subtotal: K23.90")));
        scenario.onActivity(a -> assertEquals(state(a).getProductRepository().findById("P002")
                .getHandlingInfo(), ((TextView) a.findViewById(R.id.details_handling)).getText().toString()));
        top(); capture("S05_details");
        onView(withId(R.id.details_add)).perform(scrollTo());
        capture("S05_details-handling");
        onView(withId(R.id.details_add)).perform(click());
        onView(withId(R.id.details_cart)).perform(scrollTo(), click());
        onView(product(R.id.cart_item_quantity, "P002")).check(matches(withText("Quantity: 2")));
        category("Household");
        onView(product(R.id.product_details, "P005")).perform(scrollTo(), click());
        scenario.onActivity(a -> assertEquals(state(a).getProductRepository().findById("P005")
                .getHandlingInfo(), ((TextView) a.findViewById(R.id.details_handling)).getText().toString()));
        onView(withId(R.id.header_back)).perform(click());
        onView(withId(R.id.products_heading)).check(matches(withText("Household")));
        onView(withId(R.id.header_back)).perform(click());
        capture("S03_categories");
    }

    @Test public void aggregateErrorsAndDraftRetainAcrossRecreationWithoutClearingCart() throws Exception {
        seedBasket(); checkout(); submit();
        onView(withId(R.id.checkout_name)).perform(closeSoftKeyboard());
        scenario.onActivity(a -> {
            assertEquals("Name is required.", ((TextInputLayout) a.findViewById(R.id.checkout_name_layout)).getError());
            assertEquals("Phone number is required.", ((TextInputLayout) a.findViewById(R.id.checkout_phone_layout)).getError());
            assertEquals("Location is required.", ((TextInputLayout) a.findViewById(R.id.checkout_location_layout)).getError());
            assertEquals(3085L, state(a).getCart().calculateTotalToea());
        });
        fill("Demo Shopper", "12", "");
        submit();
        onView(withId(R.id.checkout_phone)).perform(closeSoftKeyboard());
        scenario.recreate();
        onView(withId(R.id.checkout_name)).check(matches(withText("Demo Shopper")));
        onView(withId(R.id.checkout_phone)).check(matches(withText("12")));
        scenario.onActivity(a -> {
            assertNull(((TextInputLayout) a.findViewById(R.id.checkout_name_layout)).getError());
            assertNotNull(((TextInputLayout) a.findViewById(R.id.checkout_phone_layout)).getError());
            assertNotNull(((TextInputLayout) a.findViewById(R.id.checkout_location_layout)).getError());
        });
        onView(withId(R.id.checkout_name)).perform(scrollTo(), closeSoftKeyboard());
        top(); capture("S07_checkout-errors");
        onView(withId(R.id.header_back)).perform(click());
        checkout();
        onView(withId(R.id.checkout_name)).check(matches(withText("Demo Shopper")));
        onView(withId(R.id.checkout_phone)).check(matches(withText("12")));
    }

    @Test public void completeFlowUsesExactSnapshotAndNeverReturnsToSubmittedForm() throws Exception {
        seedBasket();
        onView(withId(R.id.cart_total)).perform(scrollTo());
        capture("S06_cart-total");
        checkout();
        fill(" Demo Shopper ", "+675 7000-0000", "Taraka, Lae, Morobe Province");
        onView(withId(R.id.review_total)).perform(scrollTo()).check(matches(withText("K30.85")));
        capture("S07_checkout-summary");
        onView(withId(R.id.checkout_name)).perform(scrollTo(), closeSoftKeyboard());
        top(); capture("S07_checkout");
        onView(withId(R.id.checkout_submit)).perform(scrollTo());
        capture("S07_checkout-submit");
        submit();
        onView(withId(R.id.header_title)).check(matches(withText("Order confirmation")));
        onView(withId(R.id.confirmation_name)).check(matches(withText("Demo Shopper")));
        onView(withId(R.id.confirmation_phone)).check(matches(withText("+67570000000")));
        top(); capture("S08_confirmation");
        final String[] reference = new String[1];
        scenario.onActivity(a -> {
            Order o = state(a).getLastOrder();
            reference[0] = o.getReference();
            assertTrue(reference[0].startsWith("DEMO-"));
            assertEquals(3085L, o.getTotalToea());
            assertEquals(3, o.getTotalQuantity());
            assertTrue(state(a).getCart().isEmpty());
            assertNull(((BottomNavigationView) a.findViewById(R.id.main_navigation)).getBadge(R.id.nav_cart));
        });
        scenario.recreate();
        onView(withId(R.id.confirmation_reference)).check(matches(withText(reference[0])));
        onView(withId(R.id.review_total)).perform(scrollTo()).check(matches(withText("K30.85")));
        capture("S08_confirmation-summary");
        pressBack();
        onView(withId(R.id.home_browse)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_cart)).perform(click());
        onView(withText("Your cart is empty")).check(matches(isDisplayed()));
        onView(withId(R.id.cart_checkout)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        capture("S06_empty-cart");
    }

    @Test public void changedBasketCannotSubmitAnUnreviewedTotal() {
        seedBasket(); checkout();
        fill("Demo Shopper", "70000000", "Lae, Morobe Province");
        scenario.onActivity(a -> state(a).getCart().setQuantity("P002", 3));
        scenario.recreate();
        onView(withId(R.id.review_total)).perform(scrollTo()).check(matches(withText("K30.85")));
        submit();
        onView(withId(R.id.header_title)).check(matches(withText("Cart")));
        onView(withId(R.id.cart_total)).perform(scrollTo()).check(matches(withText("K42.80")));
        checkout();
        onView(withId(R.id.checkout_name)).check(matches(withText("Demo Shopper")));
        onView(withId(R.id.review_total)).perform(scrollTo()).check(matches(withText("K42.80")));
        submit();
        scenario.onActivity(a -> assertEquals(4280L, state(a).getLastOrder().getTotalToea()));
    }

    @Test public void duplicateCallbackKeepsConfirmationAndRetryPreservesNewCart() {
        seedBasket(); checkout();
        fill("Demo Shopper", "70000000", "Lae, Morobe Province");
        scenario.onActivity(a -> {
            View button = a.findViewById(R.id.checkout_submit);
            String token = state(a).getCheckoutToken();
            button.performClick();
            Order order = state(a).getLastOrder();
            button.performClick();
            assertSame(order, state(a).getLastOrder());
            state(a).getCart().addProduct(state(a).getProductRepository().findById("P004"), 1);
            assertSame(order, state(a).placeDemoOrder(token,
                    new Customer("Demo Shopper", "70000000", "Lae, Morobe Province")));
            assertEquals(695L, state(a).getCart().calculateTotalToea());
        });
        onView(withId(R.id.confirmation_continue)).perform(scrollTo(), click());
        onView(withId(R.id.nav_cart)).perform(click());
        onView(withId(R.id.cart_total)).perform(scrollTo()).check(matches(withText("K6.95")));
    }

    @Test public void detailsAddRejectsAggregateQuantityAbove99() {
        category("Grocery");
        scenario.onActivity(a -> state(a).getCart().addProduct(
                state(a).getProductRepository().findById("P004"), 98));
        onView(product(R.id.product_details, "P004")).perform(scrollTo(), click());
        onView(withId(R.id.details_plus)).perform(scrollTo(), click());
        onView(withId(R.id.details_add)).perform(scrollTo(), click());
        scenario.onActivity(a -> assertEquals(98, state(a).getCart().getTotalQuantity()));
        onView(withId(R.id.details_minus)).perform(scrollTo(), click());
        onView(withId(R.id.details_add)).perform(scrollTo(), click());
        scenario.onActivity(a -> assertEquals(99, state(a).getCart().getTotalQuantity()));
    }

    @Test public void whitespaceAndOverlongInputsShowReachableSpecificErrors() {
        seedBasket(); checkout();
        fill(" \u00a0 ", "70000000", " \u00a0 ");
        submit();
        onView(withId(R.id.checkout_name)).perform(closeSoftKeyboard());
        scenario.onActivity(a -> {
            assertEquals("Name is required.", ((TextInputLayout) a.findViewById(R.id.checkout_name_layout)).getError());
            assertEquals("Location is required.", ((TextInputLayout) a.findViewById(R.id.checkout_location_layout)).getError());
        });
        String longName = new String(new char[101]).replace('\0', 'N');
        String longLocation = new String(new char[201]).replace('\0', 'L');
        fill(longName, "1234567890123456", longLocation);
        submit();
        onView(withId(R.id.checkout_name)).perform(closeSoftKeyboard());
        scenario.recreate();
        scenario.onActivity(a -> {
            assertEquals("Name must be at most 100 characters.",
                    ((TextInputLayout) a.findViewById(R.id.checkout_name_layout)).getError());
            assertEquals("Location must be at most 200 characters.",
                    ((TextInputLayout) a.findViewById(R.id.checkout_location_layout)).getError());
            assertNotNull(((TextInputLayout) a.findViewById(R.id.checkout_phone_layout)).getError());
            assertEquals(3085L, state(a).getCart().calculateTotalToea());
        });
        onView(withId(R.id.checkout_location)).perform(scrollTo()).check(matches(withText(longLocation)));
        onView(withId(R.id.checkout_submit)).perform(scrollTo()).check(matches(isDisplayed()));
        fill("Demo Shopper", "70000000", "Lae, Morobe Province");
        submit();
        onView(withId(R.id.header_title)).check(matches(withText("Order confirmation")));
    }
}
