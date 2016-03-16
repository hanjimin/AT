package co.favorie.at;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.customview.ATActionDetailBar;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.inapp.InAppManagerGoogle;
import co.favorie.at.preference.CharacterSession;


public class UpdateDefaultATActivity extends Activity {

    private Context context = this;
    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private ATActionDetailBar atActionDetailBar;

    private EditText title;
    private LinearLayout selecte_weeklyday;
    private Button select_sun;
    private Button select_mon;

    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;
    private int tempBuyIndex;

    private co.favorie.at.datum.ATDatumForTransmit atDatumForTransmit = co.favorie.at.datum.ATDatumForTransmit.getInstance();

    private boolean titleCompleted = false, isLifeTime = false, isWeekly = false;
    private boolean widgetStatus = true;
    private boolean notiStatus = true;
    private boolean isUpdate = false;
    private int selectedRecipe;
    private int sun_mon=1;

    private static final int SEND_BUY_REQUEST_CODE = 1001;
    private static final String VENDING_BILLING = "com.android.vending.billing.InAppBillingService.BIND";
    private IInAppBillingService mInAppService;
    private InAppManagerGoogle inAppManagerGoogle;

    private ATCharacterManager characterManager = ATCharacterManager.getInstance();

    private Typeface typeface;
    private CharacterSession mCharacterPref;

    private Button btn_remove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_default_at);
        //////////////////In App Billing Service Bind////////////////////////////
        Intent intent = new Intent(VENDING_BILLING);
        intent.setPackage("com.android.vending");
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        //////////////////Preference를 이용해서 Character 소유 관리///////////////////////////
        mCharacterPref = new CharacterSession(getApplicationContext());
        //mCharacterPref.getMyCharacterDetails();

        setStatusBarColor();

        atActionDetailBar = (ATActionDetailBar) findViewById(R.id.update_default_at_activity_action_detail_bar);
        typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        title = (EditText) findViewById(R.id.update_default_at_activity_edittext);
        title.setTypeface(typeface);
        selecte_weeklyday = (LinearLayout) findViewById(R.id.update_weeklyday_at_activity_edittext);
        select_sun = (Button) findViewById(R.id.update_sun_at_activity_bt);
        select_mon = (Button) findViewById(R.id.update_mon_at_activity_bt);

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
                        /*
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
                        */
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
        select_sun.setSelected(true);
        select_sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_mon.setSelected(false);
                select_sun.setSelected(true);
                tempATDatum.set(getString(R.string.default_weekly), new Date(), new Date(), ATScheduleDatum.WEEKLY, selectedCharIndex, widgetStatus, notiStatus);
                atDatumForTransmit.atScheduleDatum = tempATDatum;
                sun_mon = 1;
            }
        });
        select_mon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_sun.setSelected(false);
                select_mon.setSelected(true);
                tempATDatum.set(getString(R.string.default_weekly), new Date(), new Date(), ATScheduleDatum.WEEKLY_MON, selectedCharIndex, widgetStatus, notiStatus);
                atDatumForTransmit.atScheduleDatum = tempATDatum;
                sun_mon = 2;
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
                    tempBuyIndex = index;
                    startInAppBillingSenderForResult(characterManager.getCharacterList().get(index).name, index, module);
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
        isWeekly = i.getBooleanExtra("isWeekly", false);
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
            if (t.type == t.WEEKLY){
                select_mon.setSelected(false);
                select_sun.setSelected(true);
            }
            else if(t.type == t.WEEKLY_MON){
                select_mon.setSelected(true);
                select_sun.setSelected(false);
            }
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
        if (!isWeekly){
            selecte_weeklyday.setVisibility(View.GONE);
        }

        btn_remove = (Button) findViewById(R.id.btn_remove);
        btn_remove.setTypeface(typeface);

        if(i.getIntExtra("update_or_insert",0) == 0){
            btn_remove.setVisibility(View.VISIBLE);     //UPDATE 화면일 시 지우기 버튼이 보인다.
        } else {
            btn_remove.setVisibility(View.GONE);
        }

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog dialog = createRemoveDialog();
                dialog.show();
            }
        });
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

        if (mInAppService != null) {
            unbindService(mServiceConn);
        }
    }

    private android.app.AlertDialog createRemoveDialog(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(UpdateDefaultATActivity.this);

        builder.setTitle(getString(R.string.delete_alert_title));
        builder.setMessage(getString(R.string.delete_alert_contents));

        builder.setPositiveButton(getString(R.string.delete_alert_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(UpdateDefaultATActivity.this,MainActivity.class);
                i.putExtra("update_or_remove",1);   // 0 = update, 1 = remove
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.delete_alert_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        android.app.AlertDialog dialog = builder.create();
        return dialog;
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

    /**
     * In App Service Connection
     */
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mInAppService = null;
            inAppManagerGoogle = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mInAppService = IInAppBillingService.Stub.asInterface(service);
            inAppManagerGoogle = new InAppManagerGoogle(getApplicationContext(), mInAppService, typeface);
        }
    };

    /**
     * InAppBilling 요청( startIntentSenderForResult() )하고 onActivityResult에서 그 결과 값을 받는다.
     *
     * @param tempProductId
     * @param index
     * @param module
     */
    public void startInAppBillingSenderForResult(String tempProductId, final int index, final ATRecyclerViewAndWidgetModule module) {

        DetectATCharacter charHelper = new DetectATCharacter();
        final String productId = charHelper.reSettingProductId(tempProductId); // InApp 에 필요한 productId를 생성

        // 결제 Alert Dialog 커스터마이징
        android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
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

        // 무료 상품일때 버튼 TEXT 바꾸기
        if (characterManager.getCharacterList().get(index).isFreeWithBand) {
            inappBuyBtn.setText(getString(R.string.alert_free));
        }

        // 패키지 상품일 때 Alert Dialog에 캐릭터 이미지가 쌍으로 나오게 하기
        if (characterManager.getCharacterList().get(index).isPackage) {

            inappImageCharacter.setImageResource(characterManager.getCharacterList().get((index)).detailviewResourceId);
            inappImageCharacterNext.setImageResource(characterManager.getCharacterList().get(charHelper.reSettingIndexForPackage(index)).detailviewResourceId);
            inappImageCharacterNext.setVisibility(View.VISIBLE);

            // 패키지 상품이 아닐 때 Alert Dialog 에 캐릭터 이미지가 하나만 나오게 하기
        } else {
            inappImageCharacter.setImageResource(characterManager.getCharacterList().get(index).detailviewResourceId);
            inappImageCharacterNext.setVisibility(View.GONE);
        }

        final android.app.AlertDialog mAlertDialog = alertBuilder.create();
        inappBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 무료 상품
                if (characterManager.getCharacterList().get(index).isFreeWithBand) {
                    selectedCharIndex = inAppManagerGoogle.completeBuyItem(productId, module, index);
                    refreshRightButton();
                    mAlertDialog.dismiss();

                    // 유료 상품
                } else {
                    try {

                        Bundle buyIntentBundle = mInAppService.getBuyIntent(3, getPackageName(), productId, "inapp", "q+1ukjssiq+3ukiq+sis");
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        mAlertDialog.dismiss();

                        if (pendingIntent != null) {
                            // 요청보냄
                            startIntentSenderForResult(pendingIntent.getIntentSender(), SEND_BUY_REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));

                        } else {
                            int tempIndex = inAppManagerGoogle.checkOwnedItemsAndRestore(productId, index, module);
                            if(tempIndex != 1000){
                                selectedCharIndex = tempIndex;
                                refreshRightButton();
                            }
                        }

                    } catch (Exception e) {
                        Log.e("==SenderForResult==", e.getMessage());
                    }
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

                int tempIndex = inAppManagerGoogle.checkOwnedItemsAndRestore(productId, index, module);
                if(tempIndex != 1000){
                    selectedCharIndex = tempIndex;
                    refreshRightButton();
                }
                mAlertDialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });

        mAlertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        android.app.AlertDialog.Builder statusAlertBuilder = new android.app.AlertDialog.Builder(this);
        statusAlertBuilder.setCancelable(true).setTitle(getString(R.string.alert_title)).setMessage(getString(R.string.alert_message)).setPositiveButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });
        android.app.AlertDialog statusAlertDialog = statusAlertBuilder.create();

        /*
             RESPONSE_CODE	        Value is 0 if the purchase was success, error otherwise.
             INAPP_PURCHASE_DATA	A String in JSON format that contains details about the purchase order. See table 4 for a description of the JSON fields.
             INAPP_DATA_SIGNATURE	String containing the signature of the purchase data that was signed with the private key of the developer. The data signature uses the RSASSA-PKCS1-v1_5 scheme.
         */

        if (requestCode == SEND_BUY_REQUEST_CODE) {

            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {

                try {

                    JSONObject jo = new JSONObject(purchaseData);
                    String productId = jo.getString("productId");
                    selectedCharIndex = inAppManagerGoogle.completeBuyItem(productId, module, tempBuyIndex);
                    refreshRightButton();

                } catch (JSONException e) {
                    Log.e("ResultParsingError", e.getMessage());
                }

            } else if (resultCode == RESULT_CANCELED) {
                // cancel == 0
                statusAlertDialog.show();
            } else {
                statusAlertDialog.show();
            }
        }
    }
}
