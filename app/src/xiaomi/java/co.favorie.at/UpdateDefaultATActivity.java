package co.favorie.at;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Date;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.preference.CharacterSession;


public class UpdateDefaultATActivity extends Activity {

    private Context context = this;
    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private ATActionDetailBar atActionDetailBar;

    private EditText title;

    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;

    private ATDatumForTransmit atDatumForTransmit = ATDatumForTransmit.getInstance();

    private boolean titleCompleted = false, isLifeTime = false;
    private boolean widgetStatus = true;
    private boolean notiStatus = true;
    private boolean isUpdate = false;
    private int selectedRecipe;

    private ATCharacterManager characterManager = ATCharacterManager.getInstance();
    private int tempBuyIndex;
    private Typeface typeface;
    private CharacterSession mCharacterPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_default_at);
        //////////////////Preference를 이용해서 Character 소유 관리///////////////////////////
        mCharacterPref = new CharacterSession(getApplicationContext());
        //mCharacterPref.getMyCharacterDetails();

        setStatusBarColor();

        atActionDetailBar = (ATActionDetailBar) findViewById(R.id.update_default_at_activity_action_detail_bar);
        typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        title = (EditText) findViewById(R.id.update_default_at_activity_edittext);
        title.setTypeface(typeface);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (title.getText().toString().length() != 0) {
                    if (title.getText().toString().matches("\\d+")) { ////////////////////is Integer?
                        if (Integer.valueOf(title.getText().toString()) > 100) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(getString(R.string.too_old)).setCancelable(true).setPositiveButton(getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                            title.setText("100");
                        }
                        titleCompleted = true;
                    } else {
                        titleCompleted = false;
                    }
                } else {
                    titleCompleted = false;
                }
                refreshRightButton();
            }
        });

        module = (ATRecyclerViewAndWidgetModule) findViewById(R.id.update_default_at_activity_recyclerview_and_widget_module);
        int selected_char_position = getIntent().getIntExtra("selected_char_position",0);
        module.initModule(selected_char_position);
        module.setOnCharsViewCallbacks(new ATRecyclerViewAndWidgetModule.ModuleCallbacks() {
            @Override
            public void charSelected(int index) {
                if (!mCharacterPref.isOwned(characterManager.getCharacterList().get(index).name)) {
                    // 캐릭터를 소유하고 있지 않은 경우
                    startInAppBillingSenderForResult(characterManager.getCharacterList().get(index).name, index);
                } else {
                    selectedCharIndex = index;
                    refreshRightButton();
                }
            }

            @Override
            public void widgetChanged(boolean status) {
                widgetStatus = status;
                refreshRightButton();
            }

            @Override
            public void notiChanged(boolean status) {
                notiStatus = status;
                refreshRightButton();
            }
        });

        //////////////////////////////// Update //////////////////////////////////
        Intent i = getIntent();
        isUpdate = i.getBooleanExtra("isUpdate", false);
        isLifeTime = i.getBooleanExtra("isLifeTime", false);
        if (isUpdate) {




            ATDatumForTransmit datum = ATDatumForTransmit.getInstance();
            ATScheduleDatum t = new ATScheduleDatum(datum.atScheduleDatum);
            selectedCharIndex = t.selectedCharacter;
            widgetStatus = t.widgetStatus;
            notiStatus = t.notiStatus;

            atActionDetailBar.setForUpdate(t.title, getIntent().getIntExtra("colorCode", 0));
            module.setSelectedCharIndex(selectedCharIndex);
            module.setWidget(widgetStatus);
            module.setNotification(notiStatus);

            tempATDatum = new ATScheduleDatum(atDatumForTransmit.atScheduleDatum);
            if (isLifeTime && t.additionalDatum >= 0 && t.additionalDatum < 1e9) {

                if (t.additionalDatum == 0) {
                    title.setText(null);
                    titleCompleted = false;
                } else {
                    title.setText(t.additionalDatum + "");
                    titleCompleted = true;
                }
            }
        } else {
            atActionDetailBar.setForNewAT();
            selectedRecipe = i.getIntExtra("selected_recipe", -1);
            if (selectedRecipe >= 0 && selectedRecipe <= 3) {
                titleCompleted = true;
            } else if (selectedRecipe == 4) {
            }


            switch (selectedRecipe) {
                case 0:
                    tempATDatum.set(getString(R.string.default_daily), new Date(), new Date(), ATScheduleDatum.DAILY, selectedCharIndex, widgetStatus, notiStatus);
                    break;
                case 1:
                    tempATDatum.set(getString(R.string.default_weekly), new Date(), new Date(), ATScheduleDatum.WEEKLY, selectedCharIndex, widgetStatus, notiStatus);
                    break;
                case 2:
                    tempATDatum.set(getString(R.string.default_monthly), new Date(), new Date(), ATScheduleDatum.MONTHLY, selectedCharIndex, widgetStatus, notiStatus);
                    break;
                case 3:
                    tempATDatum.set(getString(R.string.default_yearly), new Date(), new Date(), ATScheduleDatum.YEARLY, selectedCharIndex, widgetStatus, notiStatus);
                    break;
                case 4:
                    tempATDatum.set(getString(R.string.default_life_time), new Date(), new Date(), ATScheduleDatum.LIFETIME, selectedCharIndex, widgetStatus, notiStatus);
                    break;
            }
            atDatumForTransmit.atScheduleDatum = tempATDatum;
            refreshRightButton();
        }
        if (!isLifeTime) {
            title.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Update Default Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void refreshRightButton() {
        if (isLifeTime) {
            atActionDetailBar.refreshDoneButton(titleCompleted, true);
        } else {
            atActionDetailBar.refreshDoneButton(true, true);
        }
        if (titleCompleted || !isLifeTime) {
            if (isLifeTime) {
                tempATDatum.set(selectedCharIndex, widgetStatus, notiStatus, Integer.valueOf(title.getText().toString()));
            } else {
                tempATDatum.set(selectedCharIndex, widgetStatus, notiStatus);
            }
            atActionDetailBar.setTransmitDatum(tempATDatum);
        }
    }

    private void setStatusBarColor(){
        // STATUS BAR COLOR Change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getStatusBarColor(getIntent().getIntExtra("colorCode", 0)));
        }
    }

    private int getStatusBarColor(int colorCode){


        if (colorCode == Color.parseColor("#ABD8CC")) {
            return Color.parseColor("#80BCAA");
        } else if (colorCode == Color.parseColor("#C8DCCF")) {
            return Color.parseColor("#9FC99F");
        } else if (colorCode == Color.parseColor("#EBEBBE")) {
            return Color.parseColor("#CECC93");
        } else if (colorCode == Color.parseColor("#FEE1A1")) {
            return Color.parseColor("#F2C977");
        } else if (colorCode == Color.parseColor("#F7D38B")) {
            return Color.parseColor("#E2AF5D");
        } else if (colorCode == Color.parseColor("#F4B98C")) {
            return Color.parseColor("#ED9B64");
        } else if (colorCode == Color.parseColor("#F29F83")) {
            return Color.parseColor("#E57D61");
        } else if (colorCode == Color.parseColor("#ED8B7E")) {
            return Color.parseColor("#E06C63");
        }

        return getResources().getColor(R.color.status_red_dark);
    }

    //////////////////////////////////In App 관련 함수///////////////////////////////////////////////////////////////
    // In App Service에 요청 보내고 onActivityResult에서 응답 받는다.
    public void startInAppBillingSenderForResult(String tempProductId, final int index) {

        DetectATCharacter charHelper = new DetectATCharacter();
        final String productId = charHelper.reSettingProductId(tempProductId);

        android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        tempBuyIndex = index; // 구매를 위한 temp value


        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.inapp_alertdialog, null);
        alertBuilder.setView(dialogView).setCancelable(false);
        ImageView inappCancelBtn = (ImageView) dialogView.findViewById(R.id.inapp_cancelBtn);
        Button inappBuyBtn = (Button) dialogView.findViewById(R.id.inapp_buyBtn);
        TextView inappRestoreBuyBtn = (TextView) dialogView.findViewById(R.id.inapp_restoreBuyBtn);
        ImageView inappImageCharacter = (ImageView) dialogView.findViewById(R.id.inapp_imageCharacter);
        ImageView inappImageCharacterNext = (ImageView) dialogView.findViewById(R.id.inapp_imageCharacter_next);


        inappRestoreBuyBtn.setTypeface(typeface);
        inappBuyBtn.setTypeface(typeface);

        if (characterManager.getCharacterList().get(index).isFreeWithBand) {
            inappBuyBtn.setText(getString(R.string.alert_free));
        }


        if (characterManager.getCharacterList().get(index).isPackage) {

            // 패키지 상품일 때..
            inappImageCharacter.setImageResource(characterManager.getCharacterList().get((index)).detailviewResourceId);
            inappImageCharacterNext.setImageResource(characterManager.getCharacterList().get(charHelper.reSettingIndexForPackage(index)).detailviewResourceId);
            inappImageCharacterNext.setVisibility(View.VISIBLE);


        } else {
            inappImageCharacter.setImageResource(characterManager.getCharacterList().get(index).detailviewResourceId);
            inappImageCharacterNext.setVisibility(View.GONE);
        }

        final android.support.v7.app.AlertDialog mAlertDialog = alertBuilder.create();
        inappBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (characterManager.getCharacterList().get(index).isFreeWithBand) {
                    completeBuyItem(productId);
                    mAlertDialog.dismiss();
                }
            }
        });
        inappCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });
        inappRestoreBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAlertDialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });

        mAlertDialog.show();
    }

    public void completeBuyItem(String productId) {
        DetectATCharacter charHelper = new DetectATCharacter();
        // package라는 문자열이 들어있으면 묶음이므로, 묶음 캐릭터 둘다 열어줘야한다.
        if (productId.contains("package")) {
            mCharacterPref.setOwned(characterManager.getCharacterList().get(tempBuyIndex).name);
            mCharacterPref.setOwned(characterManager.getCharacterList().get(charHelper.reSettingIndexForPackage(tempBuyIndex)).name);
        } else {
            mCharacterPref.setOwned(characterManager.getCharacterList().get(tempBuyIndex).name);
        }

        selectedCharIndex = tempBuyIndex;  // 구매 확정 이후 temp index 값을 전달...
        refreshRightButton();
        module.setSelectedCharIndex(selectedCharIndex);
        module.notifyAdapter();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

}
