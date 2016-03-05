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

/*
The PreferencesView object is the whole settings interface.
ATTENTION there is also a class PreferenceView, which identifies a single Setting within this class.
 */
public class PreferencesView extends ArrayAdapter<Preferences.Item> {
    //Debug tag
    private String TAG;

    private Context context;

    //Animation variables, we use the one from the MainActivity
    private Interpolator timeInterpolator;
    private int fastAnimationSpeed;

    /*
    If the preference has more possible values than this, the user gets control how to go through
    all values. If there are less, then we go to the next value and at the end reverse the direction,
    which is a nice effect for e.g. booleans.
     */
    private static final int rewindLimit = 3;

    //Custom TextView, because all Settings should be square instead of rectangle
    private class SquareTextView extends TextView {
        public SquareTextView(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //The high corresponds with the width
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    /*
    A preference view does show exactely one preference and gives the user the ability to choose
    between the different values.
     */
    private class PreferenceView {
        private Preferences.Item preferenceItem;

        private FrameLayout preferenceView;
        private ViewFlipper viewFlipper;
        private TextView recommendedSettingLabel;
        private TextView headerLabel;
        private TextView footerLabel;

        //Create a new animation object for the next animations
        private Animation getNewAnimation(boolean inAnimation, boolean reverseAnimation) {
            Animation newAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                                                            Animation.RELATIVE_TO_SELF, reverseAnimation ? (inAnimation ? -1 : 0) : (inAnimation ? 1 :  0),
                                                            Animation.RELATIVE_TO_SELF, reverseAnimation ? (inAnimation ?  0 : 1) : (inAnimation ? 0 : -1));
            newAnimation.setDuration(fastAnimationSpeed);
            newAnimation.setInterpolator(timeInterpolator);
            return newAnimation;
        }

        /*
        A PreferenceView gets a single preference as input and uses only the interface within the
        Preference.Item class to interact with the settings.
         */
        public PreferenceView(Preferences.Item preferenceItem) {
            this.preferenceItem = preferenceItem;

            //The main component of a PreferenceView is this ViewFlipper which holds all possible values
            viewFlipper = new ViewFlipper(context);

            //A wrapper to show additional information like descriptions
            preferenceView = new FrameLayout(context);
            preferenceView.addView(viewFlipper);

            loadPossibleValues();
            setOnClick();
            loadCurrentSettings();

            //The overlay holds all additional informations
            loadOverlay();
        }

        private void loadOverlay() {
            //Create views for additional informations
            recommendedSettingLabel = new TextView(context);
            headerLabel = new TextView(context);
            footerLabel = new TextView(context);

            //Add the overlay to the PreferenceView
            preferenceView.addView(recommendedSettingLabel);
            preferenceView.addView(headerLabel);
            preferenceView.addView(footerLabel);

            //Set overlay to the standard configuration
            recommendedSettingLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            headerLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            footerLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            recommendedSettingLabel.setAlpha(0f);
            headerLabel.setAlpha(0f);
            footerLabel.setAlpha(0f);

            //If the layout change we call onFlip to load the current configuration, and to react to layout changes which could destroy the layout look
            recommendedSettingLabel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    onFlip();
                }
            });

            //On long click we show the description of the current setting
            viewFlipper.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    footerLabel.animate().y(0).setDuration(fastAnimationSpeed * 2);
                    return true;
                }
            });
        }

        //Is called on start and whenever the viewFlipper is flipped
        private void onFlip() {
            //Get the active View for the active value
            TextView v = (TextView) viewFlipper.getCurrentView();

            //Initialize/animate a flip
            if (v != null) {
                //Set text layout based on the layout of the active value
                recommendedSettingLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.getTextSize() * 0.6f);
                headerLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.getTextSize() * 0.8f);
                footerLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.getTextSize() * 0.6f);

                recommendedSettingLabel.setTextColor(v.getCurrentTextColor());
                headerLabel.setTextColor(v.getCurrentTextColor());
                footerLabel.setTextColor(v.getCurrentTextColor());

                //Position the additional info based on the text baseline of the active value
                recommendedSettingLabel.setPadding(0, v.getBaseline() + 20, 0, 0);

                footerLabel.setPadding(20, v.getBaseline() + 40, 20, 0);
                footerLabel.setText(preferenceItem.getDescriptionBottom());
                footerLabel.setAlpha(0.95f);
                footerLabel.animate().y(100).setDuration(fastAnimationSpeed * 2);

                headerLabel.setPadding(0, 45, 0, 0);
                headerLabel.setAlpha(0.95f);
                headerLabel.setText(preferenceItem.getDescriptionTop());
            }

            //If the active value is the default value display the "recommended" text, if not then hide it
            if (viewFlipper.getDisplayedChild() == preferenceItem.getDefaultValue()) {
                recommendedSettingLabel.setText(context.getString(R.string.recommended));
                if (recommendedSettingLabel.getAlpha() == 0f) {
                    recommendedSettingLabel.animate().setDuration(fastAnimationSpeed).setInterpolator(timeInterpolator).alpha(0.75f).y(-20);
                }
            } else {
                if (recommendedSettingLabel.getAlpha() > 0.7f) {
                    recommendedSettingLabel.animate().setDuration(fastAnimationSpeed).setInterpolator(timeInterpolator).alpha(0f).y(20);
                }
            }

            //Save the new current value to preference
            preferenceItem.save(viewFlipper.getDisplayedChild());
        }

        private void loadCurrentSettings() {
            viewFlipper.setDisplayedChild(preferenceItem.getCurrentValue());
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
