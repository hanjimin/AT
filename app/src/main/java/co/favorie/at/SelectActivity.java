package co.favorie.at;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.customview.ATCustomActivity;
import co.favorie.at.datum.ATDatumForTransmit;


public class SelectActivity extends ATCustomActivity {
    ATDatumForTransmit atDatumForTransmit = ATDatumForTransmit.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        View customActionBar = (View) findViewById(R.id.select_activity_action_bar);
        Button leftButton = (Button) customActionBar.findViewById(R.id.at_action_detail_bar_button_left);
        Button rightButton = (Button) customActionBar.findViewById(R.id.at_action_detail_bar_button_right);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setBackgroundResource(R.drawable.button_back_red);
        rightButton.setVisibility(View.INVISIBLE);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button dateButton = (Button) findViewById(R.id.select_activity_button_date);
        dateButton.setTypeface(typeface);
        Button timeButton = (Button) findViewById(R.id.select_activity_button_time);
        timeButton.setTypeface(typeface);
        Button customButton = (Button) findViewById(R.id.select_activity_button_custom);
        customButton.setTypeface(typeface);
        Button recipeButton = (Button) findViewById(R.id.select_activity_button_recipe);
        recipeButton.setTypeface(typeface);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelectActivity.this, DateDetailActivity.class);
                i.putExtra("update_or_insert",1);   //1 = insert
                startActivityForResult(i, 1);
            }
        });
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelectActivity.this, TimeDetailActivity.class);
                i.putExtra("update_or_insert",1);   //1 = insert
                startActivityForResult(i, 1);
            }
        });
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelectActivity.this, CustomDetailActivity.class);
                i.putExtra("update_or_insert",1);   //1 = insert
                startActivityForResult(i, 1);
            }
        });
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelectActivity.this, RecipeActivity.class);
                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Select Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                if(atDatumForTransmit.completed) {
                    Intent i = getIntent();
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        }
    }

}
