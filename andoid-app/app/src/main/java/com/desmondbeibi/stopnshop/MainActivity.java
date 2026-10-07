package com.desmondbeibi.stopnshop;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.desmondbeibi.stopnshop.ui.ProductImages;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.desmondbeibi.stopnshop.data.ProductRepository;
import com.desmondbeibi.stopnshop.model.Cart;
import com.desmondbeibi.stopnshop.model.CartItem;
import com.desmondbeibi.stopnshop.model.Product;
import com.desmondbeibi.stopnshop.model.Customer;
import com.desmondbeibi.stopnshop.model.Order;
import com.desmondbeibi.stopnshop.model.OrderLine;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Map;
import com.desmondbeibi.stopnshop.session.ShoppingSession;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/** Native Java/XML navigation; all money and cart rules stay in the shared model. */
public class MainActivity extends AppCompatActivity {
    private enum Screen { HOME, CATEGORIES, PRODUCTS, CART, DETAILS, CHECKOUT, CONFIRMATION }
    private static final String STATE_SCREEN = "screen";
    private static final String STATE_CATEGORY = "category";
    private static final String STATE_PRODUCT_ORIGIN = "product_origin";
    private static final String STATE_SCROLL = "scroll_y";

    private ShoppingSession session;
    private FrameLayout container;
    private BottomNavigationView navigation;
    private Screen screen = Screen.HOME;
    private Screen productOrigin = Screen.HOME;
    private String selectedCategory;
    private String selectedProductId;
    private Screen detailsOrigin = Screen.PRODUCTS;
    private int detailsQuantity = 1;
    private int detailsOriginScroll;
    private String checkoutToken;
    private String confirmationReference;
    private String draftName = "", draftPhone = "", draftLocation = "";
    private boolean validationShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        StopNShopApplication application = (StopNShopApplication) getApplication();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Attach the draw gate after AppCompat has installed the final content root.
        splash.setKeepOnScreenCondition(() ->
                application.getLaunchSplash().shouldKeepOnScreen(SystemClock.uptimeMillis()));
        splash.setOnExitAnimationListener(provider -> provider.remove());
        session = ((StopNShopApplication) getApplication()).getShoppingSession();
        container = findViewById(R.id.screen_container);
        navigation = findViewById(R.id.main_navigation);
        // The root already reserves system-bar/cutout/IME space. Replace Material's
        // inset listener so the navigation bar does not add that space a second time.
        ViewCompat.setOnApplyWindowInsetsListener(navigation, (view, insets) -> {
            view.setPadding(0, 0, 0, 0);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return insets;
        });
        WindowInsetsControllerCompat bars = new WindowInsetsControllerCompat(getWindow(),
                findViewById(R.id.main));
        bars.setAppearanceLightStatusBars(false);
        bars.setAppearanceLightNavigationBars(true);

        if (savedInstanceState != null) {
            screen = readScreen(savedInstanceState.getString(STATE_SCREEN));
            productOrigin = readScreen(savedInstanceState.getString(STATE_PRODUCT_ORIGIN));
            if (productOrigin != Screen.HOME && productOrigin != Screen.CATEGORIES
                    && productOrigin != Screen.CART) productOrigin = Screen.CATEGORIES;
            selectedProductId = savedInstanceState.getString("selected_product");
            detailsOrigin = readScreen(savedInstanceState.getString("details_origin"));
            if (detailsOrigin != Screen.HOME && detailsOrigin != Screen.PRODUCTS) detailsOrigin = Screen.PRODUCTS;
            detailsOriginScroll = savedInstanceState.getInt("details_origin_scroll");
            detailsQuantity = Math.max(1, Math.min(99, savedInstanceState.getInt("details_quantity", 1)));
            checkoutToken = savedInstanceState.getString("checkout_token");
            confirmationReference = savedInstanceState.getString("confirmation_reference");
            draftName = savedInstanceState.getString("draft_name", "");
            draftPhone = savedInstanceState.getString("draft_phone", "");
            draftLocation = savedInstanceState.getString("draft_location", "");
            validationShown = savedInstanceState.getBoolean("validation_shown");
            selectedCategory = savedInstanceState.getString(STATE_CATEGORY);
            if (!session.getProductRepository().getCategories().contains(selectedCategory)) {
                selectedCategory = null;
            }
        }
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) navigate(Screen.HOME);
            else if (item.getItemId() == R.id.nav_categories) navigate(Screen.CATEGORIES);
            else if (item.getItemId() == R.id.nav_cart) navigate(Screen.CART);
            return true;
        });
        findViewById(R.id.header_back).setOnClickListener(view -> goBack());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (screen != Screen.HOME) goBack();
                else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
        if (session.getCheckoutToken() == null) clearDraft();
        boolean recovered = recoverUnavailableScreen();
        renderScreen(savedInstanceState == null ? 0 : savedInstanceState.getInt(STATE_SCROLL));
        if (recovered) showFeedback(getString(R.string.session_reset), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigation != null) updateCartBadge();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        captureDraft();
        super.onSaveInstanceState(outState);
        outState.putString("selected_product", selectedProductId);
        outState.putString("details_origin", detailsOrigin.name());
        outState.putInt("details_origin_scroll", detailsOriginScroll);
        outState.putInt("details_quantity", detailsQuantity);
        outState.putString("checkout_token", checkoutToken);
        outState.putString("confirmation_reference", confirmationReference);
        outState.putString("draft_name", draftName);
        outState.putString("draft_phone", draftPhone);
        outState.putString("draft_location", draftLocation);
        outState.putBoolean("validation_shown", validationShown);
        outState.putString(STATE_SCREEN, screen.name());
        outState.putString(STATE_CATEGORY, selectedCategory);
        outState.putString(STATE_PRODUCT_ORIGIN, productOrigin.name());
        outState.putInt(STATE_SCROLL, currentScrollY());
    }

    private static Screen readScreen(String value) {
        try { return Screen.valueOf(value == null ? "HOME" : value); }
        catch (IllegalArgumentException exception) { return Screen.HOME; }
    }

    private int currentScrollY() {
        ScrollView scroll = container.findViewById(R.id.screen_scroll);
        return scroll == null ? 0 : scroll.getScrollY();
    }

    private void navigate(Screen destination) {
        captureDraft();
        hideKeyboard();
        dismissFeedback();
        screen = destination;
        renderScreen(0);
    }

    private void openProducts(String category) {
        selectedCategory = category;
        productOrigin = screen;
        navigate(Screen.PRODUCTS);
    }

    private void returnFromProducts() {
        if (screen == Screen.PRODUCTS) navigate(productOrigin);
    }

    private void renderScreen(int scrollY) {
        container.removeAllViews();
        int layout = R.layout.screen_home;
        int title = R.string.brand_name;
        int navId = R.id.nav_home;
        if (screen == Screen.CATEGORIES) {
            layout = R.layout.screen_categories;
            title = R.string.nav_categories;
            navId = R.id.nav_categories;
        } else if (screen == Screen.PRODUCTS) {
            layout = R.layout.screen_products;
            title = R.string.all_products;
            navId = R.id.nav_categories;
        } else if (screen == Screen.CART) {
            layout = R.layout.screen_cart;
            title = R.string.nav_cart;
            navId = R.id.nav_cart;
        }
        if (screen == Screen.DETAILS) {
            layout = R.layout.screen_details;
            title = R.string.product_details;
            navId = detailsOrigin == Screen.HOME ? R.id.nav_home : R.id.nav_categories;
        } else if (screen == Screen.CHECKOUT) {
            layout = R.layout.screen_checkout;
            title = R.string.checkout;
            navId = R.id.nav_cart;
        } else if (screen == Screen.CONFIRMATION) {
            layout = R.layout.screen_confirmation;
            title = R.string.confirmation;
            navId = R.id.nav_home;
        }
        navigation.setVisibility(screen == Screen.CHECKOUT || screen == Screen.CONFIRMATION ? View.GONE : View.VISIBLE);
        getLayoutInflater().inflate(layout, container, true);
        ((TextView) findViewById(R.id.header_title)).setText(screen == Screen.PRODUCTS && selectedCategory != null ? selectedCategory : getString(title));
        findViewById(R.id.header_back).setVisibility(screen == Screen.PRODUCTS || screen == Screen.DETAILS || screen == Screen.CHECKOUT || screen == Screen.CONFIRMATION ? View.VISIBLE : View.GONE);
        // Checking the menu directly does not fire another navigation event.
        navigation.getMenu().findItem(navId).setChecked(true);
        if (screen == Screen.HOME) bindHome();
        else if (screen == Screen.CATEGORIES) bindCategories();
        else if (screen == Screen.PRODUCTS) bindProducts();
        else if (screen == Screen.CART) bindCart();
        else if (screen == Screen.DETAILS) bindDetails();
        else if (screen == Screen.CHECKOUT) bindCheckout();
        else bindConfirmation();
        updateCartBadge();
        ScrollView scroll = container.findViewById(R.id.screen_scroll);
        scroll.post(() -> scroll.scrollTo(0, scrollY));
    }

    private void bindHome() {
        findViewById(R.id.home_browse).setOnClickListener(view -> openProducts(null));
        findViewById(R.id.home_categories).setOnClickListener(view -> navigate(Screen.CATEGORIES));
        LinearLayout categories = findViewById(R.id.home_category_list);
        for (String category : session.getProductRepository().getCategories()) {
            MaterialButton button = (MaterialButton) getLayoutInflater()
                    .inflate(R.layout.button_home_category, categories, false);
            button.setText(category);
            button.setTag(category);
            button.setIconResource(categoryIcon(category));
            button.setOnClickListener(view -> openProducts(category));
            categories.addView(button);
        }
        LinearLayout featured = findViewById(R.id.home_products);
        for (Product product : session.getProductRepository().getProducts()) {
            if (!ProductRepository.HOUSEHOLD.equals(product.getCategory())) addProductRow(featured, product);
        }
    }

    private void bindCategories() {
        LinearLayout list = findViewById(R.id.categories_list);
        // Follow the primary Categories design; counts and products come from the repository.
        String[] order = { ProductRepository.GROCERY, ProductRepository.MEAT_AND_POULTRY,
                ProductRepository.FRESH_PRODUCE, ProductRepository.HOUSEHOLD };
        for (String category : order) {
            View row = getLayoutInflater().inflate(R.layout.row_category, list, false);
            row.setTag(category);
            ((TextView) row.findViewById(R.id.category_title)).setText(category);
            String description = getString(categoryDescription(category));
            ((TextView) row.findViewById(R.id.category_description)).setText(description);
            int count = session.getProductRepository().findByCategory(category).size();
            String countLabel = getResources().getQuantityString(R.plurals.category_product_count, count, count);
            ((TextView) row.findViewById(R.id.category_count)).setText(countLabel);
            ((ImageView) row.findViewById(R.id.category_icon)).setImageResource(categoryIcon(category));
            row.setContentDescription(category + ". " + description + ". " + countLabel);
            row.setOnClickListener(view -> openProducts(category));
            list.addView(row);
        }
    }

    private void bindProducts() {
        List<Product> products = selectedCategory == null
                ? session.getProductRepository().getProducts()
                : session.getProductRepository().findByCategory(selectedCategory);
        ((TextView) findViewById(R.id.products_heading)).setText(
                selectedCategory == null ? getString(R.string.all_products) : selectedCategory);
        ((TextView) findViewById(R.id.products_count)).setText(
                getResources().getQuantityString(R.plurals.category_product_count, products.size(), products.size()));
        LinearLayout list = findViewById(R.id.products_list);
        for (Product product : products) addProductRow(list, product);
    }

    private void addProductRow(LinearLayout list, Product product) {
        View row = getLayoutInflater().inflate(R.layout.row_product, list, false);
        row.setTag(product.getId());
        ((TextView) row.findViewById(R.id.product_name)).setText(product.getName());
        ((TextView) row.findViewById(R.id.product_category)).setText(product.getCategory());
        ((TextView) row.findViewById(R.id.product_unit)).setText(product.getUnitLabel());
        ((TextView) row.findViewById(R.id.product_price)).setText(formatKina(product.getUnitPriceToea()));
        ((TextView) row.findViewById(R.id.product_provenance)).setText(
                ProductRepository.HOUSEHOLD.equals(product.getCategory())
                        ? R.string.synthetic_sample : R.string.historical_sample);
        ProductImages.bind(row.findViewById(R.id.product_icon), product);
        ((TextView) row.findViewById(R.id.product_image_caption)).setText(ProductImages.captionFor(product));
        MaterialButton details = row.findViewById(R.id.product_details);
        details.setContentDescription(getString(R.string.details_named, product.getName()));
        details.setOnClickListener(view -> {
            selectedProductId = product.getId();
            detailsQuantity = 1;
            detailsOrigin = screen;
            detailsOriginScroll = currentScrollY();
            navigate(Screen.DETAILS);
        });
        MaterialButton add = row.findViewById(R.id.product_add);
        add.setContentDescription(getString(R.string.add_named, product.getName()));
        add.setOnClickListener(view -> {
            try {
                session.getCart().addProduct(product, 1);
                updateCartBadge();
                showFeedback(getString(R.string.added_to_cart, product.getName()), true);
            } catch (IllegalArgumentException exception) {
                showFeedback(getString(R.string.quantity_limit), true);
            }
        });
        list.addView(row);
    }

    private void bindCart() {
        Cart cart = session.getCart();
        findViewById(R.id.cart_empty).setVisibility(cart.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.cart_totals).setVisibility(cart.isEmpty() ? View.GONE : View.VISIBLE);
        ((TextView) findViewById(R.id.cart_summary)).setText(
                getString(R.string.cart_summary, cart.getTotalQuantity(), cart.getItemCount()));
        ((TextView) findViewById(R.id.cart_total)).setText(formatKina(cart.calculateTotalToea()));
        MaterialButton checkout = findViewById(R.id.cart_checkout);
        checkout.setVisibility(cart.isEmpty() ? View.GONE : View.VISIBLE);
        checkout.setOnClickListener(view -> {
            checkoutToken = session.beginCheckout();
            navigate(Screen.CHECKOUT);
        });
        MaterialButton browse = findViewById(R.id.cart_browse);
        browse.setText(cart.isEmpty() ? R.string.browse_products : R.string.continue_browsing);
        browse.setOnClickListener(view -> openProducts(null));
        LinearLayout list = findViewById(R.id.cart_items);
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            View row = getLayoutInflater().inflate(R.layout.row_cart_item, list, false);
            row.setTag(product.getId());
            ProductImages.bind(row.findViewById(R.id.cart_product_image), product);
            ((TextView) row.findViewById(R.id.cart_product_name)).setText(product.getName());
            ((TextView) row.findViewById(R.id.cart_product_price)).setText(getString(R.string.cart_unit_price,
                    formatKina(product.getUnitPriceToea()), product.getUnitLabel()));
            ((TextView) row.findViewById(R.id.cart_item_quantity)).setText(
                    getString(R.string.cart_quantity, item.getQuantity()));
            ((TextView) row.findViewById(R.id.cart_subtotal)).setText(
                    getString(R.string.line_subtotal, formatKina(item.getSubtotalToea())));
            MaterialButton minus = row.findViewById(R.id.cart_minus);
            MaterialButton plus = row.findViewById(R.id.cart_plus);
            minus.setEnabled(item.getQuantity() > CartItem.MIN_QUANTITY);
            plus.setEnabled(item.getQuantity() < CartItem.MAX_QUANTITY);
            minus.setContentDescription(getString(R.string.decrease_quantity, product.getName()));
            plus.setContentDescription(getString(R.string.increase_quantity, product.getName()));
            minus.setOnClickListener(view -> changeQuantity(product.getId(), item.getQuantity() - 1));
            plus.setOnClickListener(view -> changeQuantity(product.getId(), item.getQuantity() + 1));
            MaterialButton remove = row.findViewById(R.id.cart_remove);
            remove.setContentDescription(getString(R.string.remove_named, product.getName()));
            remove.setOnClickListener(view -> {
                int scrollY = currentScrollY();
                cart.removeProduct(product.getId());
                renderScreen(scrollY);
                showFeedback(getString(R.string.removed, product.getName()), false);
            });
            list.addView(row);
        }
    }

    private void changeQuantity(String productId, int quantity) {
        int scrollY = currentScrollY();
        session.getCart().setQuantity(productId, quantity);
        renderScreen(scrollY);
    }


    private void goBack() {
        if (screen == Screen.PRODUCTS) returnFromProducts();
        else if (screen == Screen.DETAILS) {
            navigate(detailsOrigin);
            ScrollView scroll = container.findViewById(R.id.screen_scroll);
            scroll.post(() -> scroll.scrollTo(0, detailsOriginScroll));
        } else if (screen == Screen.CHECKOUT) navigate(Screen.CART);
        else navigate(Screen.HOME);
    }

    /** Bundle navigation is meaningful only when its process-local shopping data still exists. */
    private boolean recoverUnavailableScreen() {
        if (screen == Screen.DETAILS
                && (selectedProductId == null || selectedProductId.trim().isEmpty()
                || session.getProductRepository().findById(selectedProductId) == null)) {
            screen = Screen.HOME;
            return true;
        }
        if (screen == Screen.CHECKOUT && (session.getCart().isEmpty() || checkoutToken == null
                || !checkoutToken.equals(session.getCheckoutToken()))) {
            screen = Screen.CART;
            clearDraft();
            return true;
        }
        Order order = session.getLastOrder();
        if (screen == Screen.CONFIRMATION && (order == null
                || !order.getReference().equals(confirmationReference))) {
            screen = Screen.HOME;
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        new WindowInsetsControllerCompat(getWindow(), findViewById(R.id.main))
                .hide(WindowInsetsCompat.Type.ime());
    }

    private void bindDetails() {
        Product product = session.getProductRepository().findById(selectedProductId);
        setText(R.id.details_name, product.getName());
        setText(R.id.details_category, product.getCategory());
        setText(R.id.details_unit, product.getUnitLabel());
        setText(R.id.details_price, formatKina(product.getUnitPriceToea()));
        setText(R.id.details_description, product.getDescription());
        // Runtime overriding: both product subclasses supply their own handling message.
        setText(R.id.details_handling, product.getHandlingInfo());
        ((TextView) findViewById(R.id.details_provenance)).setText(
                ProductRepository.HOUSEHOLD.equals(product.getCategory())
                        ? R.string.synthetic_sample : R.string.demo_catalogue);
        ProductImages.bind(findViewById(R.id.details_icon), product);
        ((TextView) findViewById(R.id.details_image_caption)).setText(ProductImages.captionFor(product));
        MaterialButton minus = findViewById(R.id.details_minus);
        MaterialButton plus = findViewById(R.id.details_plus);
        minus.setContentDescription(getString(R.string.decrease_quantity, product.getName()));
        plus.setContentDescription(getString(R.string.increase_quantity, product.getName()));
        minus.setOnClickListener(view -> { detailsQuantity--; updateDetailsQuantity(product); });
        plus.setOnClickListener(view -> { detailsQuantity++; updateDetailsQuantity(product); });
        findViewById(R.id.details_add).setOnClickListener(view -> {
            try {
                session.getCart().addProduct(product, detailsQuantity);
                updateCartBadge();
                showFeedback(getString(R.string.added_to_cart, product.getName()), true);
            } catch (IllegalArgumentException exception) {
                showFeedback(getString(R.string.quantity_limit), true);
            }
        });
        findViewById(R.id.details_cart).setOnClickListener(view -> navigate(Screen.CART));
        updateDetailsQuantity(product);
    }

    private void updateDetailsQuantity(Product product) {
        findViewById(R.id.details_minus).setEnabled(detailsQuantity > CartItem.MIN_QUANTITY);
        findViewById(R.id.details_plus).setEnabled(detailsQuantity < CartItem.MAX_QUANTITY);
        setText(R.id.details_quantity, getString(R.string.cart_quantity, detailsQuantity));
        String subtotal = formatKina(Math.multiplyExact(product.getUnitPriceToea(), (long) detailsQuantity));
        setText(R.id.details_subtotal, getString(R.string.line_subtotal, subtotal));
        ((MaterialButton) findViewById(R.id.details_add)).setText(getString(R.string.add_selected, subtotal));
    }

    private void captureDraft() {
        if (screen == Screen.CHECKOUT && container.findViewById(R.id.checkout_name) != null) {
            draftName = inputText(R.id.checkout_name);
            draftPhone = inputText(R.id.checkout_phone);
            draftLocation = inputText(R.id.checkout_location);
        }
    }

    private String inputText(int id) {
        return ((TextView) findViewById(id)).getText().toString();
    }

    private void clearDraft() {
        draftName = "";
        draftPhone = "";
        draftLocation = "";
        validationShown = false;
        checkoutToken = null;
    }

    private void bindCheckout() {
        setText(R.id.checkout_name, draftName);
        setText(R.id.checkout_phone, draftPhone);
        setText(R.id.checkout_location, draftLocation);
        bindReview(session.getCheckoutLines());
        if (validationShown) showFieldErrors(Customer.validate(draftName, draftPhone, draftLocation));
        findViewById(R.id.checkout_edit_cart).setOnClickListener(view -> navigate(Screen.CART));
        findViewById(R.id.checkout_submit).setOnClickListener(view -> submitDemoOrder());
    }

    private void showFieldErrors(Map<Customer.Field, String> errors) {
        ((TextInputLayout) findViewById(R.id.checkout_name_layout)).setError(errors.get(Customer.Field.NAME));
        ((TextInputLayout) findViewById(R.id.checkout_phone_layout)).setError(errors.get(Customer.Field.PHONE));
        ((TextInputLayout) findViewById(R.id.checkout_location_layout)).setError(errors.get(Customer.Field.LOCATION));
        findViewById(R.id.checkout_errors).setVisibility(errors.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void submitDemoOrder() {
        if (screen != Screen.CHECKOUT) return;
        captureDraft();
        Map<Customer.Field, String> errors = Customer.validate(draftName, draftPhone, draftLocation);
        validationShown = true;
        showFieldErrors(errors);
        if (!errors.isEmpty()) {
            int first = errors.containsKey(Customer.Field.NAME) ? R.id.checkout_name
                    : errors.containsKey(Customer.Field.PHONE) ? R.id.checkout_phone : R.id.checkout_location;
            findViewById(first).requestFocus();
            return;
        }
        MaterialButton submit = findViewById(R.id.checkout_submit);
        submit.setEnabled(false);
        try {
            Order order = session.placeDemoOrder(checkoutToken, new Customer(draftName, draftPhone, draftLocation));
            confirmationReference = order.getReference();
            navigate(Screen.CONFIRMATION);
            clearDraft();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            // Never silently refresh the review token during submit; require an explicit cart review.
            navigate(Screen.CART);
            showFeedback(getString(R.string.checkout_changed), false);
        }
    }

    private void bindConfirmation() {
        Order order = session.getLastOrder();
        setText(R.id.confirmation_reference, order.getReference());
        setText(R.id.confirmation_name, order.getCustomer().getName());
        setText(R.id.confirmation_phone, order.getCustomer().getPhone());
        setText(R.id.confirmation_location, order.getCustomer().getLocation());
        bindReview(order.getLines());
        findViewById(R.id.confirmation_continue).setOnClickListener(view -> navigate(Screen.HOME));
    }

    private void bindReview(List<OrderLine> lines) {
        LinearLayout list = findViewById(R.id.review_lines);
        long total = 0;
        int quantity = 0;
        for (OrderLine line : lines) {
            View row = getLayoutInflater().inflate(R.layout.row_review_line, list, false);
            row.setTag(line.getProductId());
            ((TextView) row.findViewById(R.id.review_product_name)).setText(line.getName());
            ((TextView) row.findViewById(R.id.review_product_units)).setText(getString(R.string.review_line,
                    line.getQuantity(), line.getUnitLabel(), formatKina(line.getUnitPriceToea())));
            ((TextView) row.findViewById(R.id.review_product_subtotal)).setText(
                    getString(R.string.line_subtotal, formatKina(line.getSubtotalToea())));
            list.addView(row);
            total = Math.addExact(total, line.getSubtotalToea());
            quantity = Math.addExact(quantity, line.getQuantity());
        }
        setText(R.id.review_count, getString(R.string.cart_summary, quantity, lines.size()));
        setText(R.id.review_total, formatKina(total));
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private String formatKina(long toea) {
        // No floating-point conversion; always use an ASCII two-decimal kina amount.
        return String.format(java.util.Locale.ROOT, getString(R.string.currency_kina), toea / 100, toea % 100);
    }

    private void updateCartBadge() {
        int units = session.getCart().getTotalQuantity();
        if (units == 0) navigation.removeBadge(R.id.nav_cart);
        else {
            BadgeDrawable badge = navigation.getOrCreateBadge(R.id.nav_cart);
            badge.setBackgroundColor(getColor(R.color.brand_primary));
            badge.setBadgeTextColor(getColor(R.color.on_primary));
            badge.setMaxCharacterCount(4);
            badge.setNumber(units);
            badge.setContentDescriptionQuantityStringsResource(R.plurals.cart_badge_units);
        }
        navigation.getMenu().findItem(R.id.nav_cart).setContentDescription(units == 0
                ? getString(R.string.cart_empty_description)
                : getString(R.string.nav_cart));
    }

    /** Feedback occupies its own layout space so it cannot intercept shopping controls. */
    private void showFeedback(String message, boolean offerCart) {
        setText(R.id.feedback_message, message);
        findViewById(R.id.feedback_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.feedback_cart).setVisibility(offerCart ? View.VISIBLE : View.GONE);
        findViewById(R.id.feedback_cart).setOnClickListener(view -> navigate(Screen.CART));
        findViewById(R.id.feedback_dismiss).setOnClickListener(view -> dismissFeedback());
    }

    private void dismissFeedback() {
        findViewById(R.id.feedback_panel).setVisibility(View.GONE);
    }

    private int categoryIcon(String category) {
        if (ProductRepository.MEAT_AND_POULTRY.equals(category)) return R.drawable.ic_meat;
        if (ProductRepository.FRESH_PRODUCE.equals(category)) return R.drawable.ic_produce;
        if (ProductRepository.HOUSEHOLD.equals(category)) return R.drawable.ic_household;
        return R.drawable.ic_grocery;
    }

    private int categoryDescription(String category) {
        if (ProductRepository.MEAT_AND_POULTRY.equals(category)) return R.string.meat_description;
        if (ProductRepository.FRESH_PRODUCE.equals(category)) return R.string.produce_description;
        if (ProductRepository.HOUSEHOLD.equals(category)) return R.string.household_description;
        return R.string.grocery_description;
    }
}

