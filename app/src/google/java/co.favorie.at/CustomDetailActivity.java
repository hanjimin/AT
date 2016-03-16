package co.favorie.at;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;


import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.customview.ATCustomActivity;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.inapp.InAppManagerGoogle;
import co.favorie.at.preference.CharacterSession;


public class CustomDetailActivity extends ATCustomActivity {

    private co.favorie.at.customview.ATActionDetailBar atActionDetailBar;

    private EditText title, start, now, end, unit;

    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;
    private int tempBuyIndex;    // 구매가 확정되지 않고 구매 프로세스 중 임시로 갖고 다니는 index

    private Context context = this;

    private boolean titleCompleted = false, timeCompleted =false, startCompleted = false, nowCompleted = false, endCompleted = false, unitCompleted = false;

    private boolean isUpdate = false;
    private int listIndexForUpdate = 0;

    private boolean widgetStatus = true;
    private boolean notiStatus = true;

    private static final int SEND_BUY_REQUEST_CODE = 1001;
    private static final String VENDING_BILLING = "com.android.vending.billing.InAppBillingService.BIND";
    private IInAppBillingService mInAppService;
    private InAppManagerGoogle inAppManagerGoogle;

    private ATCharacterManager characterManager = ATCharacterManager.getInstance();

    private Typeface typeface;
    private CharacterSession mCharacterPref;

    private Button btn_remove;

    ATDatumForTransmit datum = ATDatumForTransmit.getInstance();
    ATScheduleDatum t = new ATScheduleDatum(datum.atScheduleDatum);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_detail);
        typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        //////////////////In App Billing Service Bind////////////////////////////
        Intent intent = new Intent(VENDING_BILLING);
        intent.setPackage("com.android.vending");
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        //////////////////Preference를 이용해서 Character 소유 관리///////////////////////////
        mCharacterPref = new CharacterSession(getApplicationContext());

        atActionDetailBar = (co.favorie.at.customview.ATActionDetailBar) findViewById(R.id.custom_detail_activity_action_detail_bar);

        title = (EditText) findViewById(R.id.custom_detail_activity_title_edittext);
        title.setTypeface(typeface);

        String recipe_type = getIntent().getStringExtra("recipe_type");

        if(recipe_type == null){
            recipe_type = "common";
        }

        if(recipe_type.equals("diet")) {
            title.setText(getResources().getString(R.string.default_diet));
            titleCompleted = true;
        }

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
                    titleCompleted = true;
                } else {
                    titleCompleted = false;
                }
                refreshRightButton();
            }
        });

        start = (EditText) findViewById(R.id.custom_detail_activity_start_edittext);
        start.setTypeface(typeface);
        now = (EditText) findViewById(R.id.custom_detail_activity_now_edittext);
        now.setTypeface(typeface);
        end = (EditText) findViewById(R.id.custom_detail_activity_end_edittext);
        end.setTypeface(typeface);
        unit = (EditText) findViewById(R.id.custom_detail_activity_unit_edittext);
        unit.setTypeface(typeface);

        if(recipe_type.equals("diet")) {
            unit.setText("KG");
            unitCompleted = true;
        }

        start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (start.getText().toString().length() != 0) {
                    startCompleted = true;
                } else {
                    startCompleted = false;
                }
                refreshRightButton();
            }
        });
        now.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (now.getText().toString().length() != 0) {
                    nowCompleted = true;
                } else {
                    nowCompleted = false;
                }
                refreshRightButton();
            }
        });
        end.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (end.getText().toString().length() != 0) {
                    endCompleted = true;
                } else {
                    endCompleted = false;
                }
                refreshRightButton();
            }
        });
        unit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (unit.getText().toString().length() != 0) {
                    unitCompleted = true;
                } else {
                    unitCompleted = false;
                }
                refreshRightButton();
            }
        });

        module = (ATRecyclerViewAndWidgetModule) findViewById(R.id.date_detail_activity_recyclerview_and_widget_module);
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
        if (isUpdate) {
            ATDatumForTransmit datum = ATDatumForTransmit.getInstance();
            ATScheduleDatum t = new ATScheduleDatum(datum.atScheduleDatum);
            listIndexForUpdate = t.listIndex;
            selectedCharIndex = t.selectedCharacter;
            widgetStatus = t.widgetStatus;
            notiStatus = t.notiStatus;
            titleCompleted = true;
            timeCompleted = true;

            atActionDetailBar.setForUpdate(datum.atScheduleDatum.title, getIntent().getIntExtra("colorCode", 0));
            module.setSelectedCharIndex(selectedCharIndex);
            module.setWidget(widgetStatus);
            module.setNotification(notiStatus);
            title.setText(datum.atScheduleDatum.title);
            refreshPickedCustomTextView();
            refreshRightButton();
        } else {
            atActionDetailBar.setForNewAT();
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

                android.app.AlertDialog dialog = createRemoveDialog();
                dialog.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Time Detail Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private android.app.AlertDialog createRemoveDialog(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CustomDetailActivity.this);

        builder.setTitle(getString(R.string.delete_alert_title));
        builder.setMessage(getString(R.string.delete_alert_contents));

        builder.setPositiveButton(getString(R.string.delete_alert_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(CustomDetailActivity.this,MainActivity.class);
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
        if(startCompleted && nowCompleted && endCompleted && unitCompleted)
            timeCompleted = true;
        atActionDetailBar.refreshDoneButton(titleCompleted, timeCompleted);
        if (titleCompleted && timeCompleted) {
            tempATDatum.set(title.getText().toString(), start.getText().toString(), now.getText().toString(), end.getText().toString(), unit.getText().toString(), 2, selectedCharIndex, widgetStatus, notiStatus);
            if (isUpdate)
                tempATDatum.listIndex = listIndexForUpdate;
            atActionDetailBar.setTransmitDatum(tempATDatum);
        }
    }

    private void refreshPickedCustomTextView() {
        start.setText(t.start);
        now.setText(t.lable);
        end.setText(t.end);
        unit.setText(t.unit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mInAppService != null) {
            unbindService(mServiceConn);
        }
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
    public void startInAppBillingSenderForResult(String tempProductId, final int index , final ATRecyclerViewAndWidgetModule module) {

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
                    selectedCharIndex = inAppManagerGoogle.completeBuyItem(productId,module,index);
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
                    selectedCharIndex = inAppManagerGoogle.completeBuyItem(productId,module,tempBuyIndex);
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
