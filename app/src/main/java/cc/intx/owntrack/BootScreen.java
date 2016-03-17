package cc.intx.owntrack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/*  Aside from looking quite good the boot screen gives us also time to load all settings.
    The startup feels way smoother this way. */

public class BootScreen {
    private Activity activity;

    private FrameLayout outerWrapperLayout;

    private int animationSpeed;

    public BootScreen(Activity activity, int animationSpeed, int delay, Runnable runnable) {
        this.activity = activity;
        this.animationSpeed = animationSpeed;

        /* Wait just long enough to give the boot screen a chance to be visible */
        new Handler().postDelayed(runnable, 100);

        createOverlay();
        setCallback(delay);
    }

    private void createOverlay() {
        /* Layout which will wrap the logo text. That is a headline and a caption, but is freely expendable */
        LinearLayout mainTextWrapperLayout = new LinearLayout(activity);
        mainTextWrapperLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mainTextWrapperLayout.setOrientation(LinearLayout.VERTICAL);

        /* This outer wrapper is needed for gravity to work correctly */
        outerWrapperLayout = new FrameLayout(activity);
        outerWrapperLayout.setBackgroundColor(Misc.getColor(activity, R.color.colorPrimary));
        outerWrapperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        /* This inner wrapper is needed for gravity to work correctly */
        FrameLayout innerWrapperLayout = new FrameLayout(activity);
        innerWrapperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));


        /* create headline */
        TextView mainHeadlineTextView = new TextView(activity);
        mainHeadlineTextView.setText(activity.getString(R.string.app_name));
        mainHeadlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        mainHeadlineTextView.setTextColor(Color.WHITE);
        mainHeadlineTextView.setTypeface(Typeface.DEFAULT_BOLD);
        mainHeadlineTextView.setAlpha(0.2f);

        /* create caption */
        TextView mainCaptionTextView = new TextView(activity);
        mainCaptionTextView.setText(activity.getString(R.string.app_name_caption));
        mainCaptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mainCaptionTextView.setTextColor(Color.WHITE);
        mainCaptionTextView.setGravity(Gravity.CENTER);
        mainCaptionTextView.setTranslationY(-40);
        mainCaptionTextView.setAlpha(0.2f);


        /* Glue everything together */
        mainTextWrapperLayout.addView(mainHeadlineTextView);
        mainTextWrapperLayout.addView(mainCaptionTextView);

        innerWrapperLayout.addView(mainTextWrapperLayout);

        outerWrapperLayout.addView(innerWrapperLayout);

        /* Overlay the main activity */
        activity.addContentView(outerWrapperLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setCallback(int delayMs) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                outerWrapperLayout.animate().y(outerWrapperLayout.getMeasuredHeight()).setDuration((int)(animationSpeed * 0.6)).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        outerWrapperLayout.removeAllViews();
                    }
                }).start();
            }
        };
        handler.postDelayed(runnable, delayMs);
    }
}
