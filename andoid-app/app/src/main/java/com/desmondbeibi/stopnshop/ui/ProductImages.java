package com.desmondbeibi.stopnshop.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.desmondbeibi.stopnshop.R;
import com.desmondbeibi.stopnshop.model.Product;

/** Android presentation only: domain products remain independent of Android resources. */
public final class ProductImages {
    private static Bitmap historicalFlyer;

    private ProductImages() { }

    public static void bind(ImageView view, Product product) {
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // A fresh Drawable has its own bounds; every photo shares one unchanged source bitmap.
        Rect region = regionFor(product.getId());
        if (region != null) {
            view.setImageDrawable(new FlyerRegion(getFlyer(view.getResources()), region));
            view.setContentDescription(view.getContext().getString(
                    R.string.historical_product_image, product.getName()));
        } else if ("P005".equals(product.getId())) {
            view.setImageResource(R.drawable.ic_demo_laundry_soap);
            view.setContentDescription(view.getContext().getString(
                    R.string.synthetic_product_image, product.getName()));
        } else {
            view.setImageResource(R.drawable.ic_grocery);
            view.setContentDescription(view.getContext().getString(
                    R.string.generic_product_image, product.getName()));
        }
    }

    public static int captionFor(Product product) {
        if (regionFor(product.getId()) != null) return R.string.flyer_image_caption;
        return "P005".equals(product.getId())
                ? R.string.soap_image_caption : R.string.generic_image_caption;
    }

    /** Coordinates in the supplied 509 x 720 flyer, stored without density scaling. */
    private static Rect regionFor(String id) {
        switch (id) {
            case "P001": return new Rect(52, 204, 235, 313);
            case "P002": return new Rect(274, 207, 445, 310);
            case "P003": return new Rect(74, 389, 197, 445);
            case "P004": return new Rect(256, 368, 304, 485);
            default: return null;
        }
    }

    private static synchronized Bitmap getFlyer(Resources resources) {
        if (historicalFlyer == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            historicalFlyer = BitmapFactory.decodeResource(resources, R.drawable.specials_2018, options);
            if (historicalFlyer == null) throw new IllegalStateException("Missing historical flyer");
        }
        return historicalFlyer;
    }

    /** Canvas selects a source rectangle at draw time; no generated or edited photo is stored. */
    private static final class FlyerRegion extends Drawable {
        private final Bitmap flyer;
        private final Rect source;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        FlyerRegion(Bitmap flyer, Rect source) {
            this.flyer = flyer;
            this.source = new Rect(source);
        }

        @Override public void draw(Canvas canvas) {
            canvas.drawBitmap(flyer, source, getBounds(), paint);
        }

        @Override public int getIntrinsicWidth() { return source.width(); }
        @Override public int getIntrinsicHeight() { return source.height(); }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) {
            paint.setColorFilter(filter);
            invalidateSelf();
        }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
