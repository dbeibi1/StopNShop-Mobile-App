package com.desmondbeibi.stopnshop;

import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.session.ShoppingSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/** Exercises visible navigation, filtering and cart behaviour through actual native controls. */
@RunWith(AndroidJUnit4.class)
public class ShoppingNavigationTest {
    private ActivityScenario<MainActivity> scenario;

    @Before public void openFreshCart() {
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> session(activity).getCart().clear());
        scenario.recreate();
    }

    @After public void closeAndCleanCart() {
        if (scenario != null) {
            scenario.onActivity(activity -> session(activity).getCart().clear());
            scenario.close();
        }
    }

    private ShoppingSession session(MainActivity activity) {
        return ((StopNShopApplication) activity.getApplication()).getShoppingSession();
    }

    private Matcher<View> inProduct(int control, String productId) {
        return allOf(withId(control), isDescendantOfA(withTagValue(is((Object) productId))));
    }

    private void selectCategory(String name) {
        onView(withId(R.id.nav_categories)).perform(click());
        onView(withTagValue(is((Object) name))).perform(scrollTo(), click());
    }

    private void add(String productId) {
        onView(inProduct(R.id.product_add, productId)).perform(scrollTo(), click());
    }

    @Test public void homeBrowseAndCategoryFilteringHaveSensibleBackNavigation() {
        onView(withId(R.id.home_browse)).perform(scrollTo(), click());
        onView(withId(R.id.products_heading)).check(matches(withText("All products")));
        onView(withId(R.id.products_count)).check(matches(withText("5 sample products")));
        pressBack();
        onView(withId(R.id.home_categories)).perform(scrollTo(), click());
        onView(withTagValue(is((Object) "Household"))).perform(scrollTo(), click());
        onView(withId(R.id.products_heading)).check(matches(withText("Household")));
        onView(withId(R.id.header_title)).check(matches(withText("Household")));
        onView(inProduct(R.id.product_name, "P005")).check(matches(withText("Demo Laundry Soap")));
        onView(inProduct(R.id.product_price, "P005")).check(matches(withText("K5.00")));
        onView(withText("Zenag Kaikai")).check(doesNotExist());
        onView(withId(R.id.header_back)).perform(click());
        onView(withId(R.id.header_title)).check(matches(withText("Categories")));
        onView(withId(R.id.nav_home)).perform(click());
        onView(withId(R.id.home_browse)).check(matches(isDisplayed()));
    }

    @Test public void repeatedAddMixedBasketAndNavigationKeepAccurateTotals() {
        selectCategory("Meat & Poultry");
        onView(withId(R.id.products_count)).check(matches(withText("2 sample products")));
        onView(withText("Healthy Choice Cooking Oil")).check(doesNotExist());
        add("P002");
        add("P002");
        selectCategory("Grocery");
        add("P004");
        onView(withId(R.id.nav_cart)).perform(click());
        onView(inProduct(R.id.cart_item_quantity, "P002")).check(matches(withText("Quantity: 2")));
        onView(inProduct(R.id.cart_subtotal, "P002")).check(matches(withText("Line subtotal: K23.90")));
        onView(withId(R.id.cart_total)).perform(scrollTo()).check(matches(withText("K30.85")));
        scenario.onActivity(activity -> {
            BottomNavigationView navigation = activity.findViewById(R.id.main_navigation);
            assertEquals(3, navigation.getBadge(R.id.nav_cart).getNumber());
            assertEquals("Cart", navigation.getMenu().findItem(R.id.nav_cart).getContentDescription());
        });
        onView(withId(R.id.nav_home)).perform(click());
        onView(withId(R.id.nav_cart)).perform(click());
        onView(withId(R.id.cart_total)).perform(scrollTo()).check(matches(withText("K30.85")));
    }

    @Test public void cartControlsRecalculateAndLastRemovalRestoresBrowseRecovery() {
        selectCategory("Grocery");
        add("P004");
        onView(withId(R.id.nav_cart)).perform(click());
        onView(inProduct(R.id.cart_minus, "P004")).check(matches(not(isEnabled())));
        onView(inProduct(R.id.cart_plus, "P004")).perform(scrollTo(), click());
        onView(inProduct(R.id.cart_item_quantity, "P004")).check(matches(withText("Quantity: 2")));
        onView(withId(R.id.cart_total)).check(matches(withText("K13.90")));
        onView(inProduct(R.id.cart_minus, "P004")).perform(scrollTo(), click());
        onView(withId(R.id.cart_total)).check(matches(withText("K6.95")));
        onView(inProduct(R.id.cart_remove, "P004")).perform(scrollTo(), click());
        onView(withText("Your cart is empty")).check(matches(isDisplayed()));
        onView(withId(R.id.cart_totals)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        scenario.onActivity(activity -> assertNull(
                ((BottomNavigationView) activity.findViewById(R.id.main_navigation)).getBadge(R.id.nav_cart)));
        onView(withId(R.id.cart_browse)).perform(scrollTo(), click());
        onView(withId(R.id.products_heading)).check(matches(withText("All products")));
    }

    @Test public void recreationRetainsFilteredScreenThenVisibleCart() {
        selectCategory("Meat & Poultry");
        add("P002");
        scenario.recreate();
        onView(withId(R.id.products_heading)).check(matches(withText("Meat & Poultry")));
        onView(withText("Healthy Choice Cooking Oil")).check(doesNotExist());
        onView(withId(R.id.nav_cart)).perform(click());
        scenario.recreate();
        onView(withId(R.id.header_title)).check(matches(withText("Cart")));
        onView(inProduct(R.id.cart_item_quantity, "P002")).check(matches(withText("Quantity: 1")));
        onView(withId(R.id.cart_total)).check(matches(withText("K11.95")));
        scenario.onActivity(activity -> assertEquals(1, session(activity).getCart().getTotalQuantity()));
    }

    @Test public void quantityCapBlocksExtraAddAndDisablesIncrease() {
        selectCategory("Grocery");
        scenario.onActivity(activity -> {
            ShoppingSession state = session(activity);
            state.getCart().addProduct(state.getProductRepository().findById("P004"), 99);
        });
        add("P004");
        scenario.onActivity(activity -> assertEquals(99, session(activity).getCart().getTotalQuantity()));
        onView(withId(R.id.nav_cart)).perform(click());
        onView(inProduct(R.id.cart_plus, "P004")).check(matches(not(isEnabled())));
        onView(inProduct(R.id.cart_item_quantity, "P004")).check(matches(withText("Quantity: 99")));
        onView(withId(R.id.cart_total)).check(matches(withText("K688.05")));
    }
}


