package com.desmondbeibi.stopnshop.ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class LaunchSplashTest {
    @Test public void holdsFirstLaunchForOneSecondFromFirstFrame() {
        LaunchSplash splash = new LaunchSplash();
        assertTrue(splash.shouldKeepOnScreen(5000L));
        assertTrue(splash.shouldKeepOnScreen(5999L));
        assertFalse(splash.shouldKeepOnScreen(6000L));
    }

    @Test public void recreationUsesRemainingTimeAndLaterLaunchDoesNotRepeat() {
        LaunchSplash processSplash = new LaunchSplash();
        assertTrue(processSplash.shouldKeepOnScreen(0L));
        // A recreated Activity uses the same Application-owned object.
        assertTrue(processSplash.shouldKeepOnScreen(750L));
        assertFalse(processSplash.shouldKeepOnScreen(1000L));
        assertFalse(processSplash.shouldKeepOnScreen(90000L));
    }

    @Test public void aNewProcessHasAFreshSplash() {
        LaunchSplash previousProcess = new LaunchSplash();
        assertTrue(previousProcess.shouldKeepOnScreen(2500L));
        assertFalse(previousProcess.shouldKeepOnScreen(3500L));
        LaunchSplash newProcess = new LaunchSplash();
        assertTrue(newProcess.shouldKeepOnScreen(90000L));
        assertTrue(newProcess.shouldKeepOnScreen(90999L));
        assertFalse(newProcess.shouldKeepOnScreen(91000L));
    }
}
