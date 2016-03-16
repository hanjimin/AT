package co.favorie.at.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by bmac on 2015-08-18.
 */
public class CellLayout extends RelativeLayout {
    ///////////////////////////////////////////////////////
    // For intercept seekbar touch
    //////////////////////////////////////////////////////

    public CellLayout(Context context) {
        super(context);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("you did action down!!!!!");
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("you did action move!!!!!");
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                System.out.println("you did action hover move!!!!!");
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                System.out.println("you did action hover exit!!!!!");
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("you did action up!!!!!");
                break;
            default:
                System.out.println("you did else!!!!!");
                return super.onTouchEvent(ev);
        }
        return false;
    }
}