package cc.intx.owntrack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/*  Aside from looking quite good the boot screen gives us also time to load all settings.
    The startup feels way smoother this way. */

public class BootScreen {
    private Activity activity;

    /* Save all created elements globally for about screen creation */
    private FrameLayout outerWrapperLayout;
    private FrameLayout innerWrapperLayout;

    private int animationSpeed;

    public BootScreen(Activity activity, int animationSpeed) {
        this.activity = activity;
        this.animationSpeed = animationSpeed;

        createOverlay();
        createAbout();
    }

    public BootScreen(Activity activity, int animationSpeed, int delay, Runnable runnable) {
        this.activity = activity;
        this.animationSpeed = animationSpeed;

        /* Wait just long enough to give the boot screen a chance to be visible */
        new Handler().postDelayed(runnable, 100);

        createOverlay();
        setCallback(delay);
    }

    private void createAbout() {
        innerWrapperLayout.setTranslationY(-30);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) innerWrapperLayout.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;


        /* -------------- */
        /* New components */

        /* This inner wrapper is needed for gravity to work correctly */
        FrameLayout aboutInnerWrapperLayout = new FrameLayout(activity);
        aboutInnerWrapperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        aboutInnerWrapperLayout.setTranslationY(-60);

        /* All information should be in this gridLayout */
        GridLayout aboutLayout = new GridLayout(activity);
        GridLayout.LayoutParams aboutLayoutParams = new GridLayout.LayoutParams();
        aboutLayoutParams.setGravity(Gravity.CENTER);
        aboutLayout.setLayoutParams(aboutLayoutParams);

        /* Scroll view in case the content is too big */
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scrollView.setBackgroundColor(Misc.getColor(activity, R.color.colorPrimary));
        scrollView.setPadding(0, 120, 0, 120);

        /* Format with HTML. This is way easier for specific tasks. Warning, colors in the WebView are hardcoded. */
        WebView aboutText = new WebView(activity);
        aboutText.setBackgroundColor(Color.TRANSPARENT);
        aboutText.loadData(activity.getString(R.string.about), "text/html", "utf-8");

        /* Back button */
        TextView backButton = new TextView(activity);
        backButton.setTextColor(Misc.getColor(activity, R.color.colorAccent));
        backButton.setText("back");
        backButton.setPadding(0, 0, 0, 70);
        backButton.setGravity(Gravity.CENTER);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        backButton.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCallback(0);
            }
        });


        /* -------------------------- */
        /* Bundle everything together */

        aboutLayout.addView(backButton, new GridLayout.LayoutParams(GridLayout.spec(0, GridLayout.CENTER), GridLayout.spec(0, GridLayout.CENTER)));
        aboutLayout.addView(aboutText, new GridLayout.LayoutParams(GridLayout.spec(1, GridLayout.CENTER), GridLayout.spec(0, GridLayout.CENTER)));

        scrollView.addView(aboutLayout);
        aboutInnerWrapperLayout.addView(scrollView);

        outerWrapperLayout.addView(aboutInnerWrapperLayout);
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
        innerWrapperLayout = new FrameLayout(activity);
        innerWrapperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));


        /* Create headline */
        TextView mainHeadlineTextView = new TextView(activity);
        mainHeadlineTextView.setText(activity.getString(R.string.app_name));
        mainHeadlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        mainHeadlineTextView.setTextColor(Color.argb(180, 255, 255, 255));
        mainHeadlineTextView.setTypeface(Typeface.DEFAULT_BOLD);

        /* Create caption */
        TextView mainCaptionTextView = new TextView(activity);
        mainCaptionTextView.setText(activity.getString(R.string.app_name_caption));
        mainCaptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mainCaptionTextView.setTextColor(Color.argb(180, 255, 255, 255));
        mainCaptionTextView.setGravity(Gravity.CENTER);
        mainCaptionTextView.setTranslationY(-45);


        /* Bundle everything together */
        mainTextWrapperLayout.addView(mainHeadlineTextView);
        mainTextWrapperLayout.addView(mainCaptionTextView);
        innerWrapperLayout.addView(mainTextWrapperLayout);
        outerWrapperLayout.addView(innerWrapperLayout);

        /* Consume touch events to prevent accidentally changed settings */
        outerWrapperLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /* Don't propagate event further */
                return true;
            }
        });

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
                        ((ViewGroup) outerWrapperLayout.getParent()).removeView(outerWrapperLayout);
                    }
                }).start();
            }
        };
        handler.postDelayed(runnable, delayMs);
    }
}
