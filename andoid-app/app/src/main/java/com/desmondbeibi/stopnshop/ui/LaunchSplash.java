package com.desmondbeibi.stopnshop.ui;

/** One timed splash per app process, shared across Activity recreation. */
public final class LaunchSplash {
    public static final long DURATION_MILLIS = 1000L;
    private long firstFrameUptime = -1L;
    private boolean finished;

    /** Called on the UI thread before drawing, using monotonic SystemClock uptime. */
    public boolean shouldKeepOnScreen(long uptimeMillis) {
        if (finished) return false;
        if (firstFrameUptime < 0L) firstFrameUptime = uptimeMillis;
        if (uptimeMillis - firstFrameUptime >= DURATION_MILLIS) {
            finished = true;
            return false;
        }
        return true;
    }
}
