package co.favorie.at;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.erz.timepicker_library.TimePicker;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;
import java.util.Date;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.customview.ATCustomActivity;
import co.favorie.at.preference.CharacterSession;


public class TimeDetailActivity extends ATCustomActivity {

    private ATActionDetailBar atActionDetailBar;

    private EditText title;

    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;

    private Date savedStartTime, savedEndTime;
    private Context context = this;
    private Button pickerButton;

    private boolean titleCompleted = false, timeCompleted = false;

    private boolean isUpdate = false;
    private int listIndexForUpdate = 0;

    private boolean widgetStatus = true;
    private boolean notiStatus = true;

    private ATCharacterManager characterManager = ATCharacterManager.getInstance();
    private int tempBuyIndex;    // 구매가 확정되지 않고 구매 프로세스 중 임시로 갖고 다니는 index
    private Typeface typeface;
    private CharacterSession mCharacterPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_detail);
        typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");

        //////////////////Preference를 이용해서 Character 소유 관리///////////////////////////
        mCharacterPref = new CharacterSession(getApplicationContext());
        //mCharacterPref.getMyCharacterDetails();

        atActionDetailBar = (ATActionDetailBar) findViewById(R.id.time_detail_activity_action_detail_bar);

        title = (EditText) findViewById(R.id.time_detail_activity_title_edittext);
        title.setTypeface(typeface);

        if(getIntent().getStringExtra("recipe_type").equals("exercise")){
            title.setText("운동시간");
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

        final TimePickerDialog dialog = new TimePickerDialog();
        pickerButton = (Button) findViewById(R.id.time_detail_activity_picker_button);
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // consumeAll(); // FOR TEST
                dialog.show(getFragmentManager(), "time_picker_dialog");
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
            savedStartTime = t.startDate;
            savedEndTime = t.endDate;
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
            refreshPickedTimeTextView();
            refreshRightButton();
        } else {
            atActionDetailBar.setForNewAT();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Time Detail Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void refreshRightButton() {
        atActionDetailBar.refreshDoneButton(titleCompleted, timeCompleted);
        if (titleCompleted && timeCompleted) {
            tempATDatum.set(title.getText().toString(), savedStartTime, savedEndTime, ATScheduleDatum.TIME_TO_TIME, selectedCharIndex, widgetStatus, notiStatus);
            if (isUpdate)
                tempATDatum.listIndex = listIndexForUpdate;
            atActionDetailBar.setTransmitDatum(tempATDatum);
        }
    }

    private void refreshPickedTimeTextView() {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        c1.setTime(savedStartTime);
        c2.setTime(savedEndTime);
        String HHMM1 = ATScheduleDatum.hhmmFormatter(c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE));
        HHMM1 = HHMM1.substring(0, 2) + " : " + HHMM1.substring(3, 5);
        String HHMM2 = ATScheduleDatum.hhmmFormatter(c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));
        HHMM2 = HHMM2.substring(0, 2) + " : " + HHMM2.substring(3, 5);
        pickerButton.setText(HHMM1 + "  -  " + HHMM2);
    }

    public class TimePickerDialog extends DialogFragment {
        TimePicker startTimePicker, endTimePicker, startTimePresenter, endTimePresenter;
        Button confirmButton;
        TextView fragmentTimeTitle;
        Calendar calendar = Calendar.getInstance(), calcmp = Calendar.getInstance();

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_time_picker, container, false);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
            confirmButton = (Button) v.findViewById(R.id.fragment_time_picker_confirm_button);
            fragmentTimeTitle = (TextView) v.findViewById(R.id.fragment_time_picker_title);
            fragmentTimeTitle.setTypeface(typeface);
            confirmButton.setTypeface(typeface);
            confirmButton.setTextColor(Color.parseColor("#ffffff"));
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savedStartTime = startTimePresenter.getTime();
                    savedEndTime = endTimePresenter.getTime();
                    timeCompleted = true;
                    refreshPickedTimeTextView();
                    refreshRightButton();
                    dismiss();
                }
            });

            startTimePicker = (TimePicker) v.findViewById(R.id.fragment_time_picker_start_time_picker);
            startTimePicker.enableTwentyFourHour(true);
            startTimePresenter = (TimePicker) v.findViewById(R.id.fragment_time_picker_start_time_presenter);
            startTimePresenter.disableTouch(true);
            startTimePresenter.enableTwentyFourHour(true);

            endTimePicker = (TimePicker) v.findViewById(R.id.fragment_time_picker_end_time_picker);
            endTimePicker.enableTwentyFourHour(true);
            endTimePresenter = (TimePicker) v.findViewById(R.id.fragment_time_picker_end_time_presenter);
            endTimePresenter.disableTouch(true);
            endTimePresenter.enableTwentyFourHour(true);

            setConfirmButtonOff();
            setTimePickerListener(startTimePicker, startTimePresenter, endTimePresenter);
            setTimePickerListener(endTimePicker, endTimePresenter, startTimePresenter);
            if (timeCompleted) {
                startTimePresenter.setTime(savedStartTime);
                startTimePicker.setTime(savedStartTime);
                endTimePresenter.setTime(savedEndTime);
                endTimePicker.setTime(savedEndTime);
                setConfirmButtonOn();
            }

            return v;
        }


        void setTimePickerListener(final TimePicker tp, final TimePicker tpPresenter, final TimePicker cmp) {
            tp.setTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void timeChanged(Date date) {
                    calendar.setTime(date);
                    int selectedMinute = calendar.get(Calendar.MINUTE) / 5 * 5;
                    int selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    tpPresenter.setTime(calendar.getTime());
                    calcmp.setTime(cmp.getTime());
                    if (calendar.get(Calendar.HOUR_OF_DAY) == calcmp.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) == calcmp.get(Calendar.MINUTE)) {
                        setConfirmButtonOff();
                    } else {
                        setConfirmButtonOn();
                    }
                }
            });
        }

        void setConfirmButtonOff() {
            confirmButton.setBackgroundColor(Color.parseColor("#e6e6e6"));
            confirmButton.setText("");
            confirmButton.setEnabled(false);
        }

        void setConfirmButtonOn() {
            confirmButton.setBackgroundColor(Color.parseColor("#e64b55"));
            confirmButton.setText(getString(R.string.done));
            confirmButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


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
