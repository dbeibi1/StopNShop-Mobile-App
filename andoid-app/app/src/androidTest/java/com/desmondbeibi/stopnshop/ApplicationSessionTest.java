package com.desmondbeibi.stopnshop;

import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import com.desmondbeibi.stopnshop.ui.LaunchSplash;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.session.ShoppingSession;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/** Verifies real manifest registration and same-process Activity recreation. */
@RunWith(AndroidJUnit4.class)
public class ApplicationSessionTest {
    @Test
    public void applicationKeepsCartReviewAndOrderWhenActivityIsRecreated() {
        AtomicReference<ShoppingSession> originalSession = new AtomicReference<>();
        AtomicReference<LaunchSplash> originalSplash = new AtomicReference<>();
        AtomicReference<String> reviewToken = new AtomicReference<>();
        AtomicReference<Order> completedOrder = new AtomicReference<>();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Wait for real Home drawing; RESUMED alone can occur behind the splash.
            onView(withId(R.id.home_browse)).check(matches(isDisplayed()));
            scenario.onActivity(activity -> {
                assertTrue(activity.getApplication() instanceof StopNShopApplication);
                ShoppingSession session = ((StopNShopApplication) activity.getApplication()).getShoppingSession();
                originalSession.set(session);
                originalSplash.set(((StopNShopApplication) activity.getApplication()).getLaunchSplash());
                assertFalse(originalSplash.get().shouldKeepOnScreen(SystemClock.uptimeMillis()));
                session.getCart().clear();
                session.getCart().addProduct(session.getProductRepository().findById("P002"), 2);
                session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
                reviewToken.set(session.beginCheckout());
            });

            scenario.recreate();
            scenario.onActivity(activity -> {
                ShoppingSession session = ((StopNShopApplication) activity.getApplication()).getShoppingSession();
                assertSame(originalSession.get(), session);
                assertSame(originalSplash.get(), ((StopNShopApplication) activity.getApplication()).getLaunchSplash());
                assertFalse(originalSplash.get().shouldKeepOnScreen(SystemClock.uptimeMillis()));
                assertEquals(3085L, session.getCart().calculateTotalToea());
                assertEquals(3, session.getCart().getTotalQuantity());
                assertEquals(reviewToken.get(), session.getCheckoutToken());
                completedOrder.set(session.placeDemoOrder(reviewToken.get(),
                        new Customer("Demo Shopper", "71234567", "Lae, Morobe Province")));
                assertTrue(session.getCart().isEmpty());
            });

            scenario.recreate();
            scenario.onActivity(activity -> {
                ShoppingSession session = ((StopNShopApplication) activity.getApplication()).getShoppingSession();
                assertSame(originalSession.get(), session);
                assertSame(originalSplash.get(), ((StopNShopApplication) activity.getApplication()).getLaunchSplash());
                assertFalse(originalSplash.get().shouldKeepOnScreen(SystemClock.uptimeMillis()));
                assertSame(completedOrder.get(), session.getLastOrder());
                assertEquals(3085L, session.getLastOrder().getTotalToea());
                assertTrue(session.getCart().isEmpty());
                session.getCart().addProduct(session.getProductRepository().findById("P004"), 1);
                assertSame(completedOrder.get(), session.placeDemoOrder(reviewToken.get(),
                        new Customer("Demo Shopper", "71234567", "Lae, Morobe Province")));
                assertEquals(695L, session.getCart().calculateTotalToea());
                session.getCart().clear();
            });
        }
    }
}
