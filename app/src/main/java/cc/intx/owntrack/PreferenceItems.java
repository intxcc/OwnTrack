package cc.intx.owntrack;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ViewFlipper;
import java.util.ArrayList;

public class PreferenceItems extends ArrayAdapter<Preferences.PrefItem> {
    private String TAG;

    private Context context;
    private Interpolator animationInterpolator;
    private int fastAnimationSpeed;
    private float yMotion;
    
    private class FlipperStyle {
        private Preferences.PrefItem prefItem;
        private ViewFlipper viewFlipper;

        private int getColor(int color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getResources().getColor(color, null);
            }else {
                return context.getResources().getColor(color);
            }
        }

        private Animation getNewAnimation(boolean on, boolean reverse) {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, reverse ? (on ? -1 : 0) : (on ? 1 : 0),
                                                                                                                       Animation.RELATIVE_TO_SELF, reverse ? (on ? 0  : 1) : (on ? 0 : -1));
            animation.setDuration(fastAnimationSpeed);
            animation.setInterpolator(animationInterpolator);
            return animation;
        }
        
        public FlipperStyle(final Preferences.PrefItem prefItem) {
            this.prefItem = prefItem;

            viewFlipper = new ViewFlipper(context);
            viewFlipper.setBackgroundColor(getColor(R.color.settingsflipper_bg));

            viewFlipper.setInAnimation(getNewAnimation(true, false));
            viewFlipper.setOutAnimation(getNewAnimation(false, false));

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

                    if (customPicker >= 0) {
                        return;
                    }

                    if (f.getChildCount() < 4) {
                        if (reverse) {
                            f.showPrevious();
                        } else {
                            f.showNext();
                        }

                        if (f.getDisplayedChild() >= f.getChildCount() - 1) {
                            reverse = true;

                            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
                            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));
                        } else if (f.getDisplayedChild() <= 0) {
                            reverse = false;

                            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
                            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));
                        }
                    } else {
                        if (yMotion < -0.5) {
                            reverse = true;

                            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
                            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));

                            viewFlipper.showPrevious();
                        } else {
                            reverse = false;

                            viewFlipper.setInAnimation(getNewAnimation(true, reverse));
                            viewFlipper.setOutAnimation(getNewAnimation(false, reverse));

                            viewFlipper.showNext();
                        }
                    }
                }
            });

            int min = -1;
            int max = -1;
            ArrayList<Pair<String,?>> states = prefItem.getStates();
            for (Pair<String,?> p: states) {
                String s = p.first;

                int bgcolor = Color.MAGENTA;
                int color = Color.CYAN;

                if (prefItem.getType().equals(Boolean.class.getSimpleName())) {
                    bgcolor = (Boolean) p.second ? getColor(R.color.active_green) : getColor(R.color.settingsflipper_bg);
                    color = (Boolean) p.second ? getColor(R.color.settingsflipper_bg) : getColor(R.color.settingsflipper);
                }

                if (prefItem.getType().equals(Integer.class.getSimpleName())) {
                    if (s.equals("max")) {
                        max = (Integer) p.second;
                        continue;
                    }

                    if (s.equals("min")) {
                        min = (Integer) p.second;
                        continue;
                    }

                    color = getColor(R.color.settingsflipper_bg);
                    bgcolor = getColor(R.color.active_green);

                    int step = 0;
                    if (min != -1 && max != -1) {
                        step = 150 / (max - min);
                    }

                    float[] hsv = new float[3];
                    Color.colorToHSV(bgcolor, hsv);
                    bgcolor = Color.HSVToColor(220 - ((Integer) p.second - min) * step, hsv);
                }

                if (p.second.toString().equals((prefItem.getValue()))) {
                    addTextView(s, bgcolor, color, true);
                } else {
                    addTextView(s, bgcolor, color, false);
                }
            }

            if (prefItem.getIsCustom()) {
                pickedCustomView = addTextView("click for Custom", getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), false);

                viewFlipper.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        final ViewFlipper f = (ViewFlipper) v;

                        NumberPicker numberPicker = new NumberPicker(context);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(100);
                        numberPicker.setValue(Integer.valueOf(prefItem.getValue()));
                        numberPicker.setGravity(Gravity.CENTER);
                        viewFlipper.addView(numberPicker, f.getMeasuredHeight(), f.getMeasuredWidth());

                        numberPicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NumberPicker f = (NumberPicker) v;

                                pickedCustom = f.getValue();
                                updateCustomValue();

                                viewFlipper.removeView(f);
                                customPicker = -1;
                            }
                        });

                        customPicker = viewFlipper.indexOfChild(numberPicker);
                        viewFlipper.setDisplayedChild(customPicker);

                        return false;
                    }
                });
            }
        }

        private void updateCustomValue() {
            ((TextView) viewFlipper.getChildAt(pickedCustomView)).setText(Integer.toString(pickedCustom) + " minutes");
        }

        private int pickedCustomView = -1;
        private int pickedCustom = -1;
        private int customPicker = -1;
        private int selectedView = -1;
        private int addTextView(String text, int bg, int txt, boolean selected) {
            TextView textView = new SquareTextView(context);
            textView.setText(text);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundColor(bg);
            textView.setTextColor(txt);

            viewFlipper.addView(textView);

            if (selected) {
                selectedView = viewFlipper.indexOfChild(textView);
            }

            return viewFlipper.indexOfChild(textView);
        }

        public FrameLayout getFlipper() {
            if (selectedView >= 0) {
                viewFlipper.setDisplayedChild(selectedView);
            }

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.addView(viewFlipper);

            TextView textView = new TextView(context);
            textView.setText(prefItem.getKey());
            textView.setTextSize(11);
            textView.setPadding(0, 40, 0, 0);
            textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            textView.setBackgroundColor(Color.TRANSPARENT);
            textView.setAlpha(0.5f);
            textView.setTextColor(getColor(R.color.settingsflipper));

            frameLayout.addView(textView);


            textView = new TextView(context);
            textView.setText("Default is " + prefItem.getDefValue());
            textView.setTextSize(8);
            textView.setPadding(0, 0, 0, 40);
            textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            textView.setBackgroundColor(Color.TRANSPARENT);
            textView.setAlpha(0.5f);
            textView.setTextColor(getColor(R.color.settingsflipper));

            frameLayout.addView(textView);

            return frameLayout;
        }
    }

    private class SquareTextView extends TextView {
        public SquareTextView(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    private FrameLayout createView(Preferences.PrefItem item) {
        return (new FlipperStyle(item)).getFlipper();
    }

    public PreferenceItems(Context context, ArrayList<Preferences.PrefItem> pItems, String TAG, TimeInterpolator timeInterpolator, int fastAnimationSpeed) {
        super(context, 0, pItems);

        this.TAG = TAG;
        this.context = context;
        this.animationInterpolator = (Interpolator) timeInterpolator;
        this.fastAnimationSpeed = fastAnimationSpeed;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createView(getItem(position));
        }

        return convertView;
    }
}
