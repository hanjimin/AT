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
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.preference.CharacterSession;


public class CustomDetailActivity extends ATCustomActivity {

    private ATActionDetailBar atActionDetailBar;

    private EditText title, start, now, end, unit;

    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;

    private Context context = this;

    private boolean titleCompleted = false, timeCompleted =false, startCompleted = false, nowCompleted = false, endCompleted = false, unitCompleted = false;

    private boolean isUpdate = false;
    private int listIndexForUpdate = 0;

    private boolean widgetStatus = true;
    private boolean notiStatus = true;

    private static final int SEND_BUY_REQUEST_CODE = 1001;
    private static final String VENDING_BILLING = "com.android.vending.billing.InAppBillingService.BIND";
    private IInAppBillingService mInAppService;
    private ATCharacterManager characterManager = ATCharacterManager.getInstance();
    private int tempBuyIndex;    // 구매가 확정되지 않고 구매 프로세스 중 임시로 갖고 다니는 index
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

        atActionDetailBar = (ATActionDetailBar) findViewById(R.id.custom_detail_activity_action_detail_bar);

        title = (EditText) findViewById(R.id.custom_detail_activity_title_edittext);
        title.setTypeface(typeface);

        if(getIntent().getStringExtra("recipe_type").equals("diet")) {
            title.setText("다이어트");
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

        if(getIntent().getStringExtra("recipe_type").equals("diet")) {
            unit.setText("KG");
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

    // In App Service Connection
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mInAppService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mInAppService = IInAppBillingService.Stub.asInterface(service);

        }
    };

    //////////////////////////////////In App 관련 함수///////////////////////////////////////////////////////////////
    // In App Service에 요청 보내고 onActivityResult에서 응답 받는다.
    public void startInAppBillingSenderForResult(String tempProductId, final int index) {

        DetectATCharacter charHelper = new DetectATCharacter();
        final String productId = charHelper.reSettingProductId(tempProductId);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
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

        final AlertDialog mAlertDialog = alertBuilder.create();
        inappBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (characterManager.getCharacterList().get(index).isFreeWithBand) {
                    completeBuyItem(productId);
                    mAlertDialog.dismiss();
                } else {
                    try {

                        Bundle buyIntentBundle = mInAppService.getBuyIntent(3, getPackageName(), productId, "inapp", "q+1ukjssiq+3ukiq+sis");
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        mAlertDialog.dismiss();

                        if (pendingIntent != null) {
                            // 요청보냄
                            startIntentSenderForResult(pendingIntent.getIntentSender(), SEND_BUY_REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));

                        } else {
                            checkOwnedItemsAndRestore(productId,index);
                            /*// 결제가 막히는 예상치 못한 오류
                            final TextView message = new TextView(context);
                            final SpannableString stringWithLink = new SpannableString(getString(R.string.alert_fatal_error_message));
                            Linkify.addLinks(stringWithLink, Linkify.WEB_URLS);
                            message.setText(stringWithLink);
                            message.setMovementMethod(LinkMovementMethod.getInstance());
                            AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                            aBuilder.setTitle(getString(R.string.alert_fatal_error_title))
                                    .setView(message)
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            module.setSelectedCharIndex(selectedCharIndex);
                                            module.notifyAdapter();
                                        }
                                    });
                            AlertDialog aDialog = aBuilder.create();
                            aDialog.show();
                            alreadyPurchaseItems(productId);*/
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

                checkOwnedItemsAndRestore(productId, index);
                mAlertDialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });

        mAlertDialog.show();


    }


    // 내가 이전에 구매한 아이템인지 체크
    public void checkOwnedItemsAndRestore(String myItem, int index) {

        String tempId = myItem;
        if (characterManager.getCharacterList().get(index).isPackage) {
            // 패키지 상품일 때..
            DetectATCharacter charHelper = new DetectATCharacter();
            myItem = charHelper.reSettingProductId(characterManager.getCharacterList().get(index).name);
        }

        try {


            Bundle ownedItems = mInAppService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");

            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList =
                        ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                String continuationToken =
                        ownedItems.getString("INAPP_CONTINUATION_TOKEN");
                if (ownedSkus != null) {
                    for (int i = 0; i < ownedSkus.size(); ++i) {
                        String ownedId = ownedSkus.get(i);

                        // 구매한 상품의 정보를 이용하여 무언가를 처리
                        // 유저가 보유한 상품을 복구
                        if (myItem.equals(ownedId)) {
                            completeBuyItem(myItem);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "CANNOT FIND YOUR PURCHASE LIST", Toast.LENGTH_SHORT).show();
                }

            }else{
                // 결제가 막히는 예상치 못한 오류
                final TextView message = new TextView(context);
                final SpannableString stringWithLink = new SpannableString(getString(R.string.alert_fatal_error_message));
                Linkify.addLinks(stringWithLink, Linkify.WEB_URLS);
                message.setText(stringWithLink);
                message.setMovementMethod(LinkMovementMethod.getInstance());
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                aBuilder.setTitle(getString(R.string.alert_fatal_error_title))
                        .setView(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                module.setSelectedCharIndex(selectedCharIndex);
                                module.notifyAdapter();
                            }
                        });
                AlertDialog aDialog = aBuilder.create();
                aDialog.show();
                alreadyPurchaseItems(tempId);
            }
        } catch (Exception e) {
            Log.e("==checkOwnedItems==", e.getMessage());
        }
    }


    // 이미 구매한 아이템 일 때
    public void alreadyPurchaseItems(String productId) {
        try {
            Bundle ownedItems = mInAppService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                String tempToken = "";
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");

                    if (productId.equals(jo.getString("productId"))) {
                        tempToken = tokens[i];
                    }
                }
                final String thisToken = tempToken;
                //여기서 tokens를 모두 컨슘 해주기
                consume(thisToken);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 모두 소진(TEST용)
    public void consumeAll() {
        try {
            Bundle ownedItems = mInAppService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");
                    consume(tokens[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 소진
    public void consume(final String token) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int response = mInAppService.consumePurchase(3, getPackageName(), token);
                    //response가 0이면 성공입니다.
                    if (response == 0) {
                        Log.v("==CONSUME==", "SUCCESS");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

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


        AlertDialog.Builder statusAlertBuilder = new AlertDialog.Builder(this);
        statusAlertBuilder.setCancelable(true).setTitle(getString(R.string.alert_title)).setMessage(getString(R.string.alert_message)).setPositiveButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                module.setSelectedCharIndex(selectedCharIndex);
                module.notifyAdapter();
            }
        });
        AlertDialog statusAlertDialog = statusAlertBuilder.create();

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
                    completeBuyItem(productId);


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
