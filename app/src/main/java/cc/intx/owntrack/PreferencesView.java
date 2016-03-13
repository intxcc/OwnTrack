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

    private MainActivity.ServiceControl serviceControl;

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

                //Also for smoother transition
                if (recommendedSettingLabel.getAlpha() <= 0.1f) {
                    recommendedSettingLabel.setTextColor(v.getCurrentTextColor());
                }

                //Added for smoother transition between flipper views
                if (headerLabel.getCurrentTextColor() != v.getCurrentTextColor()) {
                    final TextView finalV = v;
                    footerLabel.animate().alpha(0f).setDuration(Math.round(fastAnimationSpeed * 0.75));
                    headerLabel.animate().alpha(0f).y(-100).setDuration(Math.round(fastAnimationSpeed * 0.75)).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            headerLabel.setTextColor(finalV.getCurrentTextColor());
                            footerLabel.setTextColor(finalV.getCurrentTextColor());

                            headerLabel.animate().alpha(1f).y(0).setDuration(Math.round(fastAnimationSpeed * 0.75));
                            footerLabel.animate().alpha(1f).setDuration(Math.round(fastAnimationSpeed * 0.75));
                        }
                    });
                } else {
                    headerLabel.setTextColor(v.getCurrentTextColor());
                    footerLabel.setTextColor(v.getCurrentTextColor());
                }

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

            //Save the new current value index to preference
            preferenceItem.save(viewFlipper.getDisplayedChild());

            if (serviceControl != null) {
                serviceControl.changedSettings();
            }
        }

        private void loadCurrentSettings() {
            //Set the displayed Value TextView to the saved preference
            viewFlipper.setDisplayedChild(preferenceItem.getCurrentValue());
        }

        //Load every possible value in its own TextView with a layout based on the value (e.g. green if active)
        private void loadPossibleValues() {
            //Initialize min/max for integer values to change the color of the tile based on the value
            int min = -1;
            int max = -1;
            int step = -1;

            //Temp variable for color operations
            float[] hsv = new float[3];

            //Go through the list of possible values
            ArrayList<String> possibleValues = preferenceItem.getPossibleValues();
            for (String s: possibleValues) {
                //If the preference is of type Integer change the layout based on the integer value
                if (preferenceItem.getDataType().equals(Integer.TYPE.toString())) {
                    //If min and max are not loaded yet, load then and calculate the step between each value
                    if (min < 0 || max < 0) {
                        min = Integer.parseInt(possibleValues.get(0));
                        max = Integer.parseInt(possibleValues.get(possibleValues.size() - 1));
                        step = 160 / (max - min);

                        //Load the color as hsv, so we can perform color operation, to change the color based on the integer value
                        Color.colorToHSV(preferenceItem.getActiveBackgroundColor(), hsv);
                    }

                    //Set the text color
                    int textColor = preferenceItem.getBackgroundColor();
                    //Set the background color. The alpha channel is based on the integer value
                    int backgroundColor = Color.HSVToColor(240 - Integer.parseInt(s) * step, hsv);

                    //Create the tile and add it to the flipper
                    valueTile(possibleValues.indexOf(s), s, backgroundColor, textColor);
                }

                //If the preference is of type boolean, change the layout based on if the value is true or false
                if (preferenceItem.getDataType().equals(Boolean.TYPE.toString())) {
                    //Use the active color if the value is true
                    int backgroundColor = possibleValues.indexOf(s) == 1 ? preferenceItem.getActiveBackgroundColor() : preferenceItem.getBackgroundColor();
                    int textColor = possibleValues.indexOf(s) == 1 ? preferenceItem.getActiveTextColor() : preferenceItem.getTextColor();

                    //Create the tile and add it to the flipper
                    valueTile(possibleValues.indexOf(s), s,  backgroundColor, textColor);
                }
            }
        }

        //Create a new tile for a value and add iz to the flipper
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

        //Set new animators based on the current flipper direction
        private void setAnimator(boolean reverse) {
            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));
        }

        /*
        yMotion is used to split the Flipper in a top and bottom half, which is used when there are
        more values than in rewind limit specified. If one presses on the top half we go to the
        previous value and to the next view if pressing the bottom half.
         */
        private float yMotion;
        private void setOnClick() {
            //Set ontouch listener to set the yMotion variable
            viewFlipper.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    yMotion = (event.getAxisValue(MotionEvent.AXIS_Y) - viewFlipper.getTop() - ((viewFlipper.getBottom() - viewFlipper.getTop()) / 2)) / (viewFlipper.getMeasuredHeight() / 2);
                    return false;
                }
            });

            viewFlipper.setOnClickListener(new View.OnClickListener() {
                //Save the direction to go throuh the values
                private boolean reverse = false;

                @Override
                public void onClick(View v) {
                    ViewFlipper f = (ViewFlipper) v;

                    //Use different mechanics if there are too much values
                    if (f.getChildCount() < rewindLimit) {
                        //Change the direction on the last/first value
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

                    //Call onFlip to perform actions like saving the current value
                    onFlip();
                }
            });
        }

        //Interface for the adapter to receive the whole preference view
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

    public void setServiceControl(MainActivity.ServiceControl serviceControl) {
        this.serviceControl = serviceControl;
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
