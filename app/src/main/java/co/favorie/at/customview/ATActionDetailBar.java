package co.favorie.at.customview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.favorie.at.R;
import co.favorie.at.datum.ATScheduleDatum;

/**
 * Created by bmac on 2015-08-24.
 */
public class ATActionDetailBar extends RelativeLayout {

    TextView ATTitle;
    ImageView centerImage;
    Button rightButton, leftButton;
    boolean isUpdate;
    Context context;
    Activity curActivity;
    co.favorie.at.datum.ATDatumForTransmit transmitDatum = co.favorie.at.datum.ATDatumForTransmit.getInstance();
/*  !!!!!!!!!!!!!!!!!!!!!!!!!!! onClickRightButton callback !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ActionDetailBarCallback callback;
    interface ActionDetailBarCallback {
        public void rightButtonClicked();
    }

    public void setOnActionDetailBarCallback(ActionDetailBarCallback a) {
        callback = a;
    }
*/
    public ATActionDetailBar(Context context) {
        super(context);
    }

    public ATActionDetailBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATActionDetailBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        this.context = getContext();
        curActivity = (Activity) context;
        ATTitle = (TextView) findViewById(R.id.at_action_detail_bar_textview);
        centerImage = (ImageView) findViewById(R.id.at_action_detail_bar_imageview);
        rightButton = (Button) findViewById(R.id.at_action_detail_bar_button_right);
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = curActivity.getIntent();
                i.putExtra("update_or_remove",0);   // 0 = update, 1 = remove
                curActivity.setResult(Activity.RESULT_OK, i);
                curActivity.finish();
            }
        });
        leftButton = (Button) findViewById(R.id.at_action_detail_bar_button_left);
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                curActivity.finish();
            }
        });
    }

    public void setForUpdate(String ATTitle, int backgroundColor) {
        init();
        isUpdate = true;
        this.setBackgroundColor(backgroundColor);
        centerImage.setVisibility(GONE);
        this.ATTitle.setText(ATTitle);
        leftButton.setBackgroundResource(R.drawable.button_back_white);
        rightButton.setBackgroundResource(R.drawable.at_actionbar_button_done_white);
    }

    public void setForNewAT() {
        init();
        isUpdate = false;
        this.setBackgroundColor(Color.parseColor("#faf0e1"));
        ATTitle.setVisibility(GONE);
        leftButton.setBackgroundResource(R.drawable.button_back_red);
        rightButton.setBackgroundResource(R.drawable.at_actionbar_button_done_gray);
        rightButton.setEnabled(false);
    }

    public void refreshDoneButton(boolean titleCompleted, boolean timeCompleted) {
        if(titleCompleted && timeCompleted) {
            rightButton.setFocusable(true);
            if (isUpdate) {
                rightButton.setBackgroundResource(R.drawable.at_actionbar_button_done_white);
                rightButton.setEnabled(true);
            } else {
                rightButton.setBackgroundResource(R.drawable.at_actionbar_button_done_red);
                rightButton.setEnabled(true);
            }
        } else {
            rightButton.setBackgroundResource(R.drawable.at_actionbar_button_done_gray);
            rightButton.setEnabled(false);
        }
    }

    public void setTransmitDatum(ATScheduleDatum datum) {
        transmitDatum.atScheduleDatum = datum;
        transmitDatum.completed = true;
    }
}
