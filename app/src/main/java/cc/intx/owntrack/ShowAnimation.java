package cc.intx.owntrack;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ShowAnimation extends Animation {
    boolean reverse;
    int targetHeight;
    View view;

    public ShowAnimation(View view, int targetHeight, boolean reverse) {
        this.view = view;
        this.targetHeight = targetHeight;
        this.reverse = reverse;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (reverse) {
            view.getLayoutParams().height = (int) (targetHeight * (1 - interpolatedTime));
        } else {
            view.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
        }

        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
