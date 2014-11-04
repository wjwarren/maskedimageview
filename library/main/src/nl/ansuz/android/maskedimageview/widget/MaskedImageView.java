package nl.ansuz.android.maskedimageview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import nl.ansuz.android.maskedimageview.R;

/**
 * An {@link ImageView} that can have a mask applied to it.
 * @author wijnand
 */
public class MaskedImageView extends ImageView {

    protected int mMaskResourceId;
    protected Drawable mMaskDrawable;
    protected Bitmap mMask;

    protected boolean mIsOutlineCompatible;
    private Paint mShaderPaint;

    /**
     * @see ImageView#ImageView(Context)
     * @param context See {@link ImageView#ImageView(Context)}.
     */
    public MaskedImageView(Context context) {
        this(context, null);
    }

    /**
     * @see ImageView#ImageView(Context, AttributeSet)
     * @param context See {@link ImageView#ImageView(Context, AttributeSet)}.
     * @param attrs See {@link ImageView#ImageView(Context, AttributeSet)}.
     */
    public MaskedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @see ImageView#ImageView(Context, AttributeSet, int)
     * @param context See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     * @param attrs See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     * @param defStyle See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     */
    public MaskedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes this class.
     * @param attributes {@link AttributeSet} - Layout attributes.
     */
    private void init(AttributeSet attributes) {
        if (attributes != null) {
            parseAttributes(attributes);
        }

        mMaskDrawable = getResources().getDrawable(mMaskResourceId);
        mIsOutlineCompatible = isDeviceSdkCompatible(Build.VERSION_CODES.LOLLIPOP)
                // TODO: Test other Drawable classes to see which provide a valid clipping mask.
                && mMaskDrawable instanceof GradientDrawable;

        if (mIsOutlineCompatible) {
            initializeOutlineLollipop();
        } else {
            initializeOutlineLegacy();
        }
    }

    /**
     * Initializes this class when using a post-Lollipop API version.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initializeOutlineLollipop() {
        // Using ScaleType.MATRIX to match pre-Lollipop clipping.
        setScaleType(ScaleType.MATRIX);
        setBackground(mMaskDrawable);

        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);
    }

    /**
     * Initializes this class when using a pre-Lollipop API version.
     */
    private void initializeOutlineLegacy() {
        if (isDeviceSdkCompatible(Build.VERSION_CODES.HONEYCOMB)) {
            // Avoid issues with hardware accelerated rendering, i.e. black background showing instead of alpha.
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * @param image {@link Bitmap} - The image to base the shader on.
     * @return {@link BitmapShader} - A new BitampShader.
     */
    private BitmapShader getShader(Bitmap image) {
        return new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    /**
     * @param sdkVersion int - The minimum required SDK level.
     * @return boolean - Whether or not the device SDK level meets the required one.
     */
    private boolean isDeviceSdkCompatible(int sdkVersion) {
        return Build.VERSION.SDK_INT >= sdkVersion;
    }

    /**
     * Parses the passed in {@link AttributeSet} and stores the values locally.
     * @param attributes {@link AttributeSet} - The attributes to parse.
     */
    private void parseAttributes(AttributeSet attributes) {
        mMaskResourceId = -1;
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.MaskedImageView,
                0, 0);

        try {
            mMaskResourceId = array.getResourceId(R.styleable.MaskedImageView_mask, -1);
        } finally {
            array.recycle();
        }

        if (mMaskResourceId < 0) {
            throw new InflateException("Mandatory 'mask' attribute not set!");
        }
    }

    /**
     * Loads the image mask.<br/>
     * <br/>
     * Bitmaps are the only primitive with which you cannot use shaders
     * unless the bitmap’s configuration is Bitmap.Config.ALPHA_8.
     */
    private void loadMask() {
        if (mMaskDrawable instanceof BitmapDrawable) {
            // Only masks from Bitmaps need some extra care.
            Bitmap bitmap = drawableToBitmap(mMaskDrawable, Bitmap.Config.ARGB_8888);
            mMask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8);
            Canvas canvas = new Canvas(mMask);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
            bitmap.recycle();
        } else {
            // Regular Shapes are fine without the extra steps.
            mMask = drawableToBitmap(mMaskDrawable, Bitmap.Config.ALPHA_8);;
        }
    }

    /**
     * Creates the shader that holds the source Bitmap.<br/>
     * Used in case Outline isn't available.
     */
    private void createShaderPaint() {
        Bitmap image = drawableToBitmap(getDrawable(), Bitmap.Config.ARGB_8888);
        Shader shader = getShader(image);

        mShaderPaint = new Paint();
        mShaderPaint.setAntiAlias(true);
        mShaderPaint.setShader(shader);
    }

    /**
     * Converts a {@link Drawable} to a {@link Bitmap}.
     * @param drawable {@link Drawable} - The {@link Drawable} to convert.
     * @param config {@link Bitmap.Config} - Bitmap config to use.
     * @return {@link Bitmap} - The resulting {@link Bitmap}.
     */
    private Bitmap drawableToBitmap(Drawable drawable, Bitmap.Config config) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int bitmapWidth = drawable.getIntrinsicWidth();
        int bitmapHeight = drawable.getIntrinsicHeight();
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            // The mask may not have a width/height set, use the same as the image.
            bitmapWidth = getWidth();
            bitmapHeight = getHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, config);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Draws the "Outlined" or "masked" image for pre-Lollipop devices.
     * @param canvas {@link Canvas} - The Canvas to paint on.
     */
    private void drawOutlineLegacy(Canvas canvas) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            // Bail out early when there is nothing to do.
            super.onDraw(canvas);
            return;
        }

        if (mMask == null) {
            loadMask();
        }

        canvas.drawBitmap(mMask, 0.0f, 0.0f, mShaderPaint);
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            /** {@inheritDoc} */
            @Override
            public void onGlobalLayout() {
                createShaderPaint();

                if (isDeviceSdkCompatible(Build.VERSION_CODES.JELLY_BEAN)) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsOutlineCompatible) {
            super.onDraw(canvas);
        } else {
            drawOutlineLegacy(canvas);
        }
    }

}
