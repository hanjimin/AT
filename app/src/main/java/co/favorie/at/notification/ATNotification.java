package co.favorie.at.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import co.favorie.at.R;

/**
 * Created by bmac on 2015-11-03.
 */
public class ATNotification extends Button {


    private boolean status = true;

    public ATNotification(Context context) {
        super(context);
    }

    public ATNotification(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATNotification(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNotification(boolean status) {
        this.status = status;
        if (status)
            setBackgroundResource(R.drawable.widget_button_show);
        else
            setBackgroundResource(R.drawable.widget_button_hide);
    }

    public void changeStatus() {
        status = !status;
        if (status)
            setBackgroundResource(R.drawable.widget_button_show);
        else
            setBackgroundResource(R.drawable.widget_button_hide);
    }

    public boolean getStatus() {
        return status;
    }

}
