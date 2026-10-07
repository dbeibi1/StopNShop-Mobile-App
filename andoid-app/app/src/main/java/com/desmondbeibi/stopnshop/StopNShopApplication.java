package com.desmondbeibi.stopnshop;

import android.app.Application;

import com.desmondbeibi.stopnshop.session.ShoppingSession;
import com.desmondbeibi.stopnshop.ui.LaunchSplash;

/** Android owns one Application per process; Activities obtain the same session from it. */
public final class StopNShopApplication extends Application {
    private final ShoppingSession shoppingSession = new ShoppingSession();
    private final LaunchSplash launchSplash = new LaunchSplash();

    public LaunchSplash getLaunchSplash() {
        return launchSplash;
    }

    public ShoppingSession getShoppingSession() {
        return shoppingSession;
    }
}
