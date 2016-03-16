package co.favorie.at.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import co.favorie.at.MainActivity;

/**
 * Created by bmac on 2015-11-05.
 */
public class ATCustomTextView extends TextView {


    public ATCustomTextView(Context context) {
        super(context);
        set(null);
    }

    public ATCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        set(attrs);
    }

    public ATCustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        set(attrs);
    }



    // set custom font
    private void set(AttributeSet attrs) {
        try {
            if (this.getTypeface().getStyle() == Typeface.BOLD)
                this.setTypeface(MainActivity.BLACK_NOTO);
            else
                this.setTypeface(MainActivity.BOLD_NOTO);
        }catch (Exception e) {
        }
    }

}
