package cc.intx.owntrack;

import android.content.Context;
import android.widget.TextView;

/* Custom TextView, because all Settings should be square instead of rectangle */
public class SquareTextView extends TextView {
    public SquareTextView(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            /* The high corresponds with the width */
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
