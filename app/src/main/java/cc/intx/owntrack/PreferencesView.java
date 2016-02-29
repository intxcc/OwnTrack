package cc.intx.owntrack;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import java.util.ArrayList;

public class PreferencesView extends ArrayAdapter<Preferences.Item> {
    private String TAG;

    private Context context;

    private Interpolator timeInterpolator;
    private int fastAnimationSpeed;

    private static final int rewindLimit = 3;

    private class SquareTextView extends TextView {
        public SquareTextView(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    private class PreferenceView {
        private Preferences.Item preferenceItem;

        private FrameLayout preferenceView;
        private ViewFlipper viewFlipper;
        private TextView overlay;

        private Animation getNewAnimation(boolean inAnimation, boolean reverseAnimation) {
            Animation newAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                                                            Animation.RELATIVE_TO_SELF, reverseAnimation ? (inAnimation ? -1 : 0) : (inAnimation ? 1 :  0),
                                                            Animation.RELATIVE_TO_SELF, reverseAnimation ? (inAnimation ?  0 : 1) : (inAnimation ? 0 : -1));
            newAnimation.setDuration(fastAnimationSpeed);
            newAnimation.setInterpolator(timeInterpolator);
            return newAnimation;
        }

        public PreferenceView(Preferences.Item preferenceItem) {
            this.preferenceItem = preferenceItem;

            viewFlipper = new ViewFlipper(context);

            preferenceView = new FrameLayout(context);
            preferenceView.addView(viewFlipper);

            loadPossibleValues();
            setOnClick();
            loadCurrentSettings();

            loadOverlay();
        }

        private void loadOverlay() {
            overlay = new TextView(context);
            preferenceView.addView(overlay);
            overlay.setGravity(Gravity.CENTER_HORIZONTAL);
            overlay.setAlpha(0f);

            overlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    onFlip();
                }
            });
        }

        private void onFlip() {
            TextView v = (TextView) viewFlipper.getCurrentView();
            if (v != null) {
                overlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.getTextSize() * 0.6f);
                overlay.setTextColor(v.getCurrentTextColor());
                overlay.setPadding(0, v.getBaseline() + 20, 0, 0);
            }

            if (viewFlipper.getDisplayedChild() == preferenceItem.getDefaultValue()) {
                overlay.setText("recommended");
                if (overlay.getAlpha() == 0f) {
                    overlay.animate().setDuration(fastAnimationSpeed).setInterpolator(timeInterpolator).alpha(0.75f).y(-20);
                }
            } else {
                if (overlay.getAlpha() > 0.7f) {
                    overlay.animate().setDuration(fastAnimationSpeed).setInterpolator(timeInterpolator).alpha(0f).y(20);
                }
            }
        }

        private void loadCurrentSettings() {
            viewFlipper.setDisplayedChild(preferenceItem.getPossibleValues().indexOf(preferenceItem.getCurrentValue()));
        }

        private void loadPossibleValues() {
            int min = -1;
            int max = -1;
            int step = -1;
            float[] hsv = new float[3];
            ArrayList<String> possibleValues = preferenceItem.getPossibleValues();
            for (String s: possibleValues) {
                if (preferenceItem.getDataType().equals(Integer.TYPE.toString())) {
                    if (min < 0 || max < 0) {
                        min = Integer.parseInt(possibleValues.get(0));
                        max = Integer.parseInt(possibleValues.get(possibleValues.size() - 1));
                        step = 160 / (max - min);

                        Color.colorToHSV(preferenceItem.getActiveBackgroundColor(), hsv);
                    }

                    int textColor = preferenceItem.getBackgroundColor();
                    int backgroundColor = Color.HSVToColor(240 - Integer.parseInt(s) * step, hsv);

                    valueTile(possibleValues.indexOf(s), s, backgroundColor, textColor);
                }

                if (preferenceItem.getDataType().equals(Boolean.TYPE.toString())) {
                    int backgroundColor = possibleValues.indexOf(s) == 1 ? preferenceItem.getActiveBackgroundColor() : preferenceItem.getBackgroundColor();
                    int textColor = possibleValues.indexOf(s) == 1 ? preferenceItem.getActiveTextColor() : preferenceItem.getTextColor();

                    valueTile(possibleValues.indexOf(s), s,  backgroundColor, textColor);
                }
            }
        }

        private void valueTile(int value, String text,  int backgroundColor, int textColor) {
            text = text + preferenceItem.getValueSuffix();

            TextView valueTile = new SquareTextView(context);
            valueTile.setText(text);
            valueTile.setGravity(Gravity.CENTER);
            valueTile.setBackgroundColor(backgroundColor);
            valueTile.setTextColor(textColor);
            valueTile.setTag(value);

            viewFlipper.addView(valueTile);
        }

        private void setAnimator(boolean reverse) {
            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));
        }

        private float yMotion;
        private void setOnClick() {
            viewFlipper.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    yMotion = (event.getAxisValue(MotionEvent.AXIS_Y) - viewFlipper.getTop() - ((viewFlipper.getBottom() - viewFlipper.getTop()) / 2)) / (viewFlipper.getMeasuredHeight() / 2);
                    return false;
                }
            });

            viewFlipper.setOnClickListener(new View.OnClickListener() {
                private boolean reverse = false;

                @Override
                public void onClick(View v) {
                    ViewFlipper f = (ViewFlipper) v;

                    if (f.getChildCount() < rewindLimit) {
                        if (f.getDisplayedChild() >= f.getChildCount() - 1) {
                            reverse = true;
                            setAnimator(true);
                        } else if (f.getDisplayedChild() <= 0) {
                            reverse = false;
                            setAnimator(false);
                        }

                        if (reverse) {
                            f.showPrevious();
                        } else {
                            f.showNext();
                        }
                    } else {
                        if (yMotion < -0.5) {
                            reverse = true;
                            setAnimator(true);

                            viewFlipper.showPrevious();
                        } else {
                            reverse = false;
                            setAnimator(false);

                            viewFlipper.showNext();
                        }
                    }

                    onFlip();
                }
            });
        }

        public FrameLayout getView() {
            return preferenceView;
        }
    }

    public PreferencesView(Context context, ArrayList<Preferences.Item> preferenceItems, String TAG, TimeInterpolator timeInterpolator, int fastAnimationSpeed) {
        super(context, 0, preferenceItems);

        this.TAG = TAG;
        this.context = context;
        this.timeInterpolator = (Interpolator) timeInterpolator;
        this.fastAnimationSpeed = fastAnimationSpeed;
    }

    private FrameLayout createView(Preferences.Item preferenceItem) {
        return (new PreferenceView(preferenceItem)).getView();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createView(getItem(position));
        }

        return convertView;
    }
}
