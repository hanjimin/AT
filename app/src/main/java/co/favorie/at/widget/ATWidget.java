package co.favorie.at.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import co.favorie.at.R;

/**
 * Created by bmac on 2015-08-21.
 */
public class ATWidget extends Button {
    private boolean status = true;
    public ATWidget(Context context) {
        super(context);
    }
    public ATWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ATWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWidget(boolean status) {
        this.status = status;
        if(status)
            setBackgroundResource(R.drawable.switch_on);
        else
            setBackgroundResource(R.drawable.switch_off);
    }
    public void changeStatus() {
        status = !status;
        if(status)
            setBackgroundResource(R.drawable.switch_on);
        else
            setBackgroundResource(R.drawable.switch_off);
    }
    public boolean getStatus(){
        return status;
    }
}
