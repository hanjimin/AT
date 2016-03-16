package co.favorie.at;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.customview.ATCustomFragmentActivity;
import co.favorie.at.preference.CharacterSession;


public class DateDetailActivity extends ATCustomFragmentActivity {


    private ATActionDetailBar atActionDetailBar;

    private EditText title;

    private ATScheduleDatum tempATDatum = new ATScheduleDatum();

    private FragmentManager fm = getSupportFragmentManager();
    private CaldroidAlertFragment caldroidFragment;
    private CaldroidAlertFragment2 caldroidFragment2;

    private Date startDate = null, endDate = null;
    // firstly clicked date(from date) == startDate, finally clicked date(to date) == endDate
    private Date savedStartDate, savedEndDate;
    private Context context = this;
    private Button pickerBtnPeriod, pickerBtnDate;
    boolean isPickerPresenting = false;


    private ATRecyclerViewAndWidgetModule module;
    private int selectedCharIndex = 0;

    private boolean titleCompleted = false, dateCompleted = false;
    private boolean widgetStatus = true;
    private boolean notiStatus = true;

    private boolean isUpdate = false;
    private int listIndexForUpdate = 0;

    private ATCharacterManager characterManager = ATCharacterManager.getInstance();
    private int tempBuyIndex;
    private Typeface typeface;
    private CharacterSession mCharacterPref;

    // TAB HOST
    private String TAB_PERIOD;
    private String TAB_DATE;
    private TabHost tabHost;

    private int tabIndex = 1;
    private int datePreafter = 0;
    private int confirmCheck = 0;

    private Button btn_remove;

    public int cur_year, cur_year_temp;
    public int cur_month, cur_month_temp;

    public int cur_tab_index = 1;

    ATDatumForTransmit datum = ATDatumForTransmit.getInstance();
    ATScheduleDatum t = new ATScheduleDatum(datum.atScheduleDatum);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_detail);
        typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        TAB_PERIOD = getString(R.string.tab_widget_period);
        TAB_DATE = getString(R.string.tab_widget_date);
        confirmCheck = 0;
        //////////////////Preference를 이용해서 Character 소유 관리///////////////////////////
        mCharacterPref = new CharacterSession(getApplicationContext());
        //mCharacterPref.getMyCharacterDetails();

        atActionDetailBar = (ATActionDetailBar) findViewById(R.id.date_detail_activity_action_detail_bar);
        title = (EditText) findViewById(R.id.date_detail_activity_title_edittext);
        title.setTypeface(typeface);

        if(getIntent().getStringExtra("recipe_type").equals("exam")){
            title.setText("시험");
        } else if(getIntent().getStringExtra("recipe_type").equals("love")){
            title.setText(" ♥ ");
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

        setupTabHost();

        setupTab(new TextView(this), TAB_PERIOD);
        setupTab(new TextView(this), TAB_DATE);

        if(getIntent().getStringExtra("recipe_type").equals("exam") || getIntent().getStringExtra("recipe_type").equals("love")){
            tabHost.setCurrentTab(1);
        } else {
            tabHost.setCurrentTab(0);
        }

        pickerBtnPeriod = (Button) findViewById(R.id.tab_content_period);
        pickerBtnDate = (Button) findViewById(R.id.tab_content_date);

        pickerBtnPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cur_tab_index = 1;

                if (isPickerPresenting) {
                    return;
                }
                startDate = endDate = null;
                isPickerPresenting = true;
                caldroidFragment = new CaldroidAlertFragment();
                Bundle args = new Bundle();
                final Calendar cal = Calendar.getInstance();
                args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
                cur_year = cal.get(Calendar.YEAR);
                cur_month = cal.get(Calendar.MONTH) + 1;
                caldroidFragment.setArguments(args);
//                caldroidFragment.setTextColorForDate(R.color.calendar_selected_color, date);
                final CaldroidListener caldroidListener = new CaldroidListener() {
                    @Override
                    public void onCaldroidViewCreated() {

                        cur_year = cal.get(Calendar.YEAR);
                        cur_month = cal.get(Calendar.MONTH) + 1;
                        cur_year_temp = cal.get(Calendar.YEAR);
                        cur_month_temp = cal.get(Calendar.MONTH) + 1;

                        TextView txt_year = caldroidFragment.txt_select_year;
                        txt_year.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(getApplicationContext(), "you click year!@!@", Toast.LENGTH_SHORT).show();
                                CustomDialog dialog_year = new CustomDialog(context);
                                dialog_year.show();
                            }
                        });
                    }

                    @Override
                    public void onSelectDate(Date date, View view) {
                        dateCompleted = false;
                        if (startDate != null && endDate != null) {
                            for (Date i = startDate; i.getTime() <= endDate.getTime(); i.setTime(i.getTime() + 24 * 60 * 60 * 1000))
                                caldroidFragment.setBackgroundResourceForDate(R.color.calendar_not_selected_color, i);
                            caldroidFragment.clearSelectedDates();
                            caldroidFragment.setSelectedDates(date, date);
                            caldroidFragment.setBackgroundResourceForDate(R.color.calendar_selected_color, date);
                            startDate = date;
                            endDate = null;
                            caldroidFragment.setConfirmButtonOff();
                        } else if (startDate == null) {
                            caldroidFragment.setBackgroundResourceForDate(R.color.calendar_selected_color, date);
                            caldroidFragment.setSelectedDates(date, date);
                            startDate = date;
                            endDate = null;
                            caldroidFragment.setConfirmButtonOff();
                        } else {
                            if (startDate.compareTo(date) > 0) {
                                endDate = startDate;
                                startDate = date;
                            } else if (startDate.compareTo(date) <= 0) {
                                endDate = date;
                            }
                            Date i = new Date(startDate.getTime());
                            Date to = new Date(endDate.getTime());
                            for (; i.getTime() <= to.getTime(); i.setTime(i.getTime() + 24 * 60 * 60 * 1000))
                                caldroidFragment.setBackgroundResourceForDate(R.color.calendar_selected_color, i);
                            caldroidFragment.setSelectedDates(startDate, endDate);
                            caldroidFragment.setConfirmButtonOn();
                            dateCompleted = true;
                        }
                        caldroidFragment.refreshView();
                    }
                };
                caldroidFragment.setCaldroidListener(caldroidListener);

                startDate = null;
                caldroidFragment.show(fm, "date_picker_dialog");
            }
        });

        // SELECT BUTTON
        pickerBtnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cur_tab_index = 2;

                if (isPickerPresenting) {
                    return;
                }
                startDate = endDate = null;
                isPickerPresenting = true;
                caldroidFragment2 = new CaldroidAlertFragment2();
                Bundle args = new Bundle();
                final Calendar cal = Calendar.getInstance();
                args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
                cur_year = cal.get(Calendar.YEAR);
                cur_month = cal.get(Calendar.MONTH) + 1;
                caldroidFragment2.setArguments(args);
//                caldroidFragment.setTextColorForDate(R.color.calendar_selected_color, date);
                final CaldroidListener caldroidListener = new CaldroidListener() {

                    @Override
                    public void onCaldroidViewCreated() {

                        cur_year = cal.get(Calendar.YEAR);
                        cur_month = cal.get(Calendar.MONTH) + 1;
                        cur_year_temp = cal.get(Calendar.YEAR);
                        cur_month_temp = cal.get(Calendar.MONTH) + 1;

                        TextView txt_year = caldroidFragment2.txt_select_year;
                        txt_year.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(getApplicationContext(), "you click year!@!@", Toast.LENGTH_SHORT).show();
                                CustomDialog dialog_year = new CustomDialog(context);
                                dialog_year.show();
                            }
                        });
                    }

                    @Override
                    public void onSelectDate(Date date, View view) {
                        dateCompleted = false;
                        if (startDate != null) {
                            caldroidFragment2.clearSelectedDates();
                            caldroidFragment2.setBackgroundResourceForDate(R.color.calendar_not_selected_color, startDate);
                            caldroidFragment2.setSelectedDates(date, date);
                            caldroidFragment2.setBackgroundResourceForDate(R.color.calendar_selected_color, date);
                            startDate = date;
                            endDate = null;
                            caldroidFragment2.setConfirmButtonOn();
                            dateCompleted = true;
                        } else if (startDate == null) {
                            caldroidFragment2.setBackgroundResourceForDate(R.color.calendar_selected_color, date);
                            caldroidFragment2.setSelectedDates(date, date);
                            startDate = date;
                            endDate = null;
                            caldroidFragment2.setConfirmButtonOn();
                            dateCompleted = true;
                        }
                        caldroidFragment2.refreshView();
                    }
                };
                caldroidFragment2.setCaldroidListener(caldroidListener);

                startDate = null;
                caldroidFragment2.show(fm, "date_picker_dialog");
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
            startDate = t.startDate;
            endDate = t.endDate;
            listIndexForUpdate = t.listIndex;
            selectedCharIndex = t.selectedCharacter;
            widgetStatus = t.widgetStatus;
            notiStatus = t.notiStatus;
            tabIndex = t.tabIndex;
            dateCompleted = true;
            titleCompleted = true;
            datePreafter = t.datePreafter;

            atActionDetailBar.setForUpdate(t.title, getIntent().getIntExtra("colorCode", 0));
            module.setSelectedCharIndex(selectedCharIndex);
            module.setWidget(widgetStatus);
            module.setNotification(notiStatus);
            title.setText(t.title);
            if (tabIndex == 1) {
                refreshPickedDateTextView();
            } else if (tabIndex == 2) {
                refreshSelectDateTextView();
            }
            refreshRightButton();
        } else {
            atActionDetailBar.setForNewAT();
        }

        btn_remove = (Button) findViewById(R.id.btn_remove);
        btn_remove.setTypeface(typeface);

        if (i.getIntExtra("update_or_insert", 0) == 0) {
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

    //////////////////////////////////연도, 월 선택하는 dialog/////////////////////////
    public class CustomDialog extends Dialog implements View.OnClickListener {
        Button btn_ok;
        ListView list_year;
        ListView list_month;
        YearListAdapter year_adapter;
        MonthListAdapter month_adapter;

        public CustomDialog(Context context) {
            super(context);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.caldroid_select_year);

            list_year = (ListView) findViewById(R.id.list_year);
            list_month = (ListView) findViewById(R.id.list_month);

            year_adapter = new YearListAdapter();
            list_year.setAdapter(year_adapter);

            for (int i = 1900; i <= 2100; i++) {
                year_adapter.add(String.valueOf(i));
            }

            list_year.setDivider(null);
            list_year.setSelected(true);
            list_year.setSelection(cur_year - 1900);
            list_year.setItemChecked(cur_year - 1900, true);
            list_year.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            list_year.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(getContext(), year_adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                    cur_year_temp = Integer.parseInt(year_adapter.getItem(position).toString());
                    for (int j = 0; j < parent.getChildCount(); j++)
                        parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                    // change the background color of the selected element
                    view.setBackgroundColor(getResources().getColor(R.color.selected_year_month_bg));
                }
            });

            month_adapter = new MonthListAdapter();
            list_month.setAdapter(month_adapter);

            for (int i = 1; i <= 12; i++) {
                month_adapter.add(String.valueOf(i));
            }

            list_month.setDivider(null);
            list_month.setSelected(true);
            list_month.setSelection(cur_month - 1);
            list_month.setItemChecked(cur_month - 1, true);
            list_month.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

            list_month.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(getContext(), month_adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                    cur_month_temp = Integer.parseInt(month_adapter.getItem(position).toString());
                    for (int j = 0; j < parent.getChildCount(); j++)
                        parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                    // change the background color of the selected element
                    view.setBackgroundColor(getResources().getColor(R.color.selected_year_month_bg));
                }
            });

            btn_ok = (Button) findViewById(R.id.btn_ok);
            btn_ok.setOnClickListener(this);
        }

        public class YearListAdapter extends BaseAdapter {

            // 문자열을 보관 할 ArrayList
            private ArrayList<String> m_List;

            // 생성자
            public YearListAdapter() {
                m_List = new ArrayList<String>();
            }

            // 현재 아이템의 수를 리턴
            @Override
            public int getCount() {
                return m_List.size();
            }

            // 현재 아이템의 오브젝트를 리턴, Object를 상황에 맞게 변경하거나 리턴받은 오브젝트를 캐스팅해서 사용
            @Override
            public Object getItem(int position) {
                return m_List.get(position);
            }

            // 아이템 position의 ID 값 리턴
            @Override
            public long getItemId(int position) {
                return position;
            }

            // 출력 될 아이템 관리
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int pos = position;
                final Context context = parent.getContext();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.select_year_layout, parent, false);

                cur_year_temp = cur_year;

                // TextView에 현재 position의 문자열 추가
                TextView text = (TextView) convertView.findViewById(R.id.txt_year);
                text.setText(m_List.get(position));

                return convertView;
            }

            // 외부에서 아이템 추가 요청 시 사용
            public void add(String _msg) {
                m_List.add(_msg);
            }

            // 외부에서 아이템 삭제 요청 시 사용
            public void remove(int _position) {
                m_List.remove(_position);
            }
        }

        public class MonthListAdapter extends BaseAdapter {

            // 문자열을 보관 할 ArrayList
            private ArrayList<String> m_List;

            // 생성자
            public MonthListAdapter() {
                m_List = new ArrayList<String>();
            }

            // 현재 아이템의 수를 리턴
            @Override
            public int getCount() {
                return m_List.size();
            }

            // 현재 아이템의 오브젝트를 리턴, Object를 상황에 맞게 변경하거나 리턴받은 오브젝트를 캐스팅해서 사용
            @Override
            public Object getItem(int position) {
                return m_List.get(position);
            }

            // 아이템 position의 ID 값 리턴
            @Override
            public long getItemId(int position) {
                return position;
            }

            // 출력 될 아이템 관리
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int pos = position;
                final Context context = parent.getContext();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.select_month_layout, parent, false);

                cur_month_temp = cur_month;

                // TextView에 현재 position의 문자열 추가
                TextView text = (TextView) convertView.findViewById(R.id.txt_month);
                text.setText(m_List.get(position));

                return convertView;
            }

            // 외부에서 아이템 추가 요청 시 사용
            public void add(String _msg) {
                m_List.add(_msg);
            }

            // 외부에서 아이템 삭제 요청 시 사용
            public void remove(int _position) {
                m_List.remove(_position);
            }
        }

        public void onClick(View view) {
            if (view == btn_ok) {
                Calendar cal = Calendar.getInstance();
                cal.set(cur_year_temp, cur_month_temp - 1, 1);
                if(cur_tab_index == 1){
                    caldroidFragment.moveToDate(cal.getTime());
                } else {
                    caldroidFragment2.moveToDate(cal.getTime());
                }

                dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Date Detail Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private android.app.AlertDialog createRemoveDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DateDetailActivity.this);

        builder.setTitle(getString(R.string.delete_alert_title));
        builder.setMessage(getString(R.string.delete_alert_contents));

        builder.setPositiveButton(getString(R.string.delete_alert_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(DateDetailActivity.this, MainActivity.class);
                i.putExtra("update_or_remove", 1);   // 0 = update, 1 = remove
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
        atActionDetailBar.refreshDoneButton(titleCompleted, dateCompleted);
        if (titleCompleted && dateCompleted) {
            if (endDate == null) {
                tabIndex = 2;
                if (startDate.compareTo(new Date()) > 0) {// 오늘 이후를 골랏을 경우
                    confirmCheck = 1;
                    datePreafter = 1;
                    endDate = new Date();
                    tempATDatum.set(title.getText().toString(), endDate, startDate, ATScheduleDatum.DATE_TO_DATE, selectedCharIndex, widgetStatus, notiStatus, tabIndex, datePreafter);
                } else {// 오늘을 포함 이전을 골랏을 경우
                    datePreafter = 0;
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(startDate);
                    Date currDate = setDateToCalendar(new Date());
                    double durationDay = (currDate.getTime() - startDate.getTime())/(3600*1000*24);
                    int anniversary = (int)((Math.floor((durationDay/100))+1)*100);
                    int q = (int)Math.floor(anniversary/365);
                    if(anniversary-100 < durationDay && durationDay < 365*q){
                        anniversary=  365*q;
                    }
                    calEnd.add(Calendar.DATE, anniversary);
                    endDate = calEnd.getTime();
                    tempATDatum.set(title.getText().toString(), startDate, endDate, ATScheduleDatum.DATE_TO_DATE, selectedCharIndex, widgetStatus, notiStatus, tabIndex, datePreafter);
                }
            } else if (tabIndex == 2) {

                if (confirmCheck == 1) {
                    tempATDatum.set(title.getText().toString(), endDate, startDate, ATScheduleDatum.DATE_TO_DATE, selectedCharIndex, widgetStatus, notiStatus, tabIndex, datePreafter);
                } else{
                    tempATDatum.set(title.getText().toString(), startDate, endDate, ATScheduleDatum.DATE_TO_DATE, selectedCharIndex, widgetStatus, notiStatus, tabIndex, datePreafter);
                }

            } else {
                datePreafter = 0;
                tempATDatum.set(title.getText().toString(), startDate, endDate, ATScheduleDatum.DATE_TO_DATE, selectedCharIndex, widgetStatus, notiStatus, tabIndex, datePreafter);
            }
            if (isUpdate)
                tempATDatum.listIndex = listIndexForUpdate;
            atActionDetailBar.setTransmitDatum(tempATDatum);
        }
    }

    private Date setDateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setCalendarHHMMSSToZero(calendar);
        Date mDate = new Date(calendar.getTimeInMillis());
        return mDate;
    }
    private void setCalendarHHMMSSToZero(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public void refreshPickedDateTextView() {
        savedStartDate = startDate;
        savedEndDate = endDate;
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startDate);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(endDate);
        String startStr, endStr;
        Locale l = new Locale("eng");
        startStr = c1.get(Calendar.DAY_OF_MONTH) + " " + new DateFormatSymbols(l).getShortMonths()[c1.get(Calendar.MONTH)] + " " + c1.get(Calendar.YEAR);
        endStr = c2.get(Calendar.DAY_OF_MONTH) + " " + new DateFormatSymbols(l).getShortMonths()[c2.get(Calendar.MONTH)] + " " + c2.get(Calendar.YEAR);
        pickerBtnPeriod.setText(startStr + "  -  " + endStr);
        tabHost.setCurrentTab(0);
    }

    public void refreshSelectDateTextView() {
        savedStartDate = startDate;
        Calendar c1 = Calendar.getInstance();
        if (endDate != null) {
            if (t.datePreafter == 1) {
                c1.setTime(endDate);
            } else {
                c1.setTime(startDate);
            }
        } else {
            c1.setTime(startDate);
        }
        String selectStr;

        Locale l = new Locale("eng");
        selectStr = c1.get(Calendar.DAY_OF_MONTH) + " " + new DateFormatSymbols(l).getShortMonths()[c1.get(Calendar.MONTH)] + " " + c1.get(Calendar.YEAR);
        pickerBtnDate.setText(selectStr);
        tabHost.setCurrentTab(1);
    }

    public class CaldroidAlertFragment extends CaldroidFragment {
        Button confirmButton;
        TextView statusIndicator;
        TextView txt_select_year;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = super.onCreateView(inflater, container, savedInstanceState);
            confirmButton = new Button(context);
            confirmButton.setTypeface(typeface);
            confirmButton.setTextColor(Color.parseColor("#ffffff"));
            confirmButton.setTextSize(18);
            confirmButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())));
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabIndex = 1;
                    refreshPickedDateTextView();
                    refreshRightButton();
                    dismiss();
                }
            });
            statusIndicator = new TextView(context);
            statusIndicator.setTypeface(typeface);
            statusIndicator.setTextColor(Color.parseColor("#ffffff"));
            statusIndicator.setTextSize(18);
            statusIndicator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 66, getResources().getDisplayMetrics())));
            statusIndicator.setGravity(Gravity.CENTER);
            statusIndicator.setBackgroundColor(Color.parseColor("#ed8b7e"));

            txt_select_year = new TextView(context);
            txt_select_year.setTypeface(typeface);
            txt_select_year.setTextColor(Color.parseColor("#ffffff"));
            txt_select_year.setTextSize(18);
            txt_select_year.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())));
            txt_select_year.setGravity(Gravity.CENTER);
            txt_select_year.setBackgroundColor(Color.parseColor("#efc9af"));

            LinearLayout l = new LinearLayout(context);

            l.setOrientation(LinearLayout.VERTICAL);
            l.setGravity(Gravity.CENTER_HORIZONTAL);
            l.addView(statusIndicator);
            l.addView(txt_select_year);
            l.addView(v);
            l.addView(confirmButton);
            setConfirmButtonOff();
            statusIndicator.setText(getString(R.string.when_start_date));
            txt_select_year.setText(getString(R.string.select_year));
            return l;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            isPickerPresenting = false;
        }


        public void setConfirmButtonOn() {
            confirmButton.setBackgroundColor(Color.parseColor("#e64b55"));
            confirmButton.setText(getString(R.string.done));
            confirmButton.setEnabled(true);
            statusIndicator.setText(getString(R.string.great));
        }

        public void setConfirmButtonOff() {
            confirmButton.setBackgroundColor(Color.parseColor("#e6e6e6"));
            confirmButton.setText(getString(R.string.done));
            confirmButton.setEnabled(false);
            statusIndicator.setText(getString(R.string.when_end_date));
        }
    }

    public class CaldroidAlertFragment2 extends CaldroidFragment {
        Button confirmButton;
        TextView statusIndicator;
        TextView txt_select_year;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = super.onCreateView(inflater, container, savedInstanceState);
            confirmButton = new Button(context);
            confirmButton.setTypeface(typeface);
            confirmButton.setTextColor(Color.parseColor("#ffffff"));
            confirmButton.setTextSize(18);
            confirmButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())));
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabIndex = 2;
                    refreshSelectDateTextView();
                    refreshRightButton();
                    dismiss();
                }
            });
            statusIndicator = new TextView(context);
            statusIndicator.setTypeface(typeface);
            statusIndicator.setTextColor(Color.parseColor("#ffffff"));
            statusIndicator.setTextSize(18);
            statusIndicator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 66, getResources().getDisplayMetrics())));
            statusIndicator.setGravity(Gravity.CENTER);
            statusIndicator.setBackgroundColor(Color.parseColor("#ed8b7e"));

            txt_select_year = new TextView(context);
            txt_select_year.setTypeface(typeface);
            txt_select_year.setTextColor(Color.parseColor("#ffffff"));
            txt_select_year.setTextSize(18);
            txt_select_year.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())));
            txt_select_year.setGravity(Gravity.CENTER);
            txt_select_year.setBackgroundColor(Color.parseColor("#efc9af"));

            LinearLayout l = new LinearLayout(context);

            l.setOrientation(LinearLayout.VERTICAL);
            l.setGravity(Gravity.CENTER_HORIZONTAL);
            l.addView(statusIndicator);
            l.addView(txt_select_year);
            l.addView(v);
            l.addView(confirmButton);
            setConfirmButtonOff();
            statusIndicator.setText(getString(R.string.when_select_date));
            txt_select_year.setText(getString(R.string.select_year));
            return l;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            isPickerPresenting = false;
        }

        public void setConfirmButtonOn() {
            confirmButton.setBackgroundColor(Color.parseColor("#e64b55"));
            confirmButton.setText(getString(R.string.done));
            confirmButton.setEnabled(true);
            statusIndicator.setText(getString(R.string.great));
        }

        public void setConfirmButtonOff() {
            confirmButton.setBackgroundColor(Color.parseColor("#e6e6e6"));
            confirmButton.setText(getString(R.string.done));
            confirmButton.setEnabled(false);
            statusIndicator.setText(getString(R.string.when_end_date));
        }
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

    private void setupTabHost() {
        // xml resource에서 TabHost를 받아왔다면 setup()을 수행해주어야함.
        tabHost = (TabHost) findViewById(R.id.date_detail_tabHost);
        tabHost.setup();
        tabHost.getTabWidget().setDividerDrawable(null);
    }

    private void setupTab(final View view, String tag) {
        View tabview = createTabView(tabHost.getContext(), tag);

        // TabSpec은 공개된 생성자가 없으므로 직접 생성할 수 없으며, TabHost의 newTabSpec메서드로 생성
        TabHost.TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview);

        if (tag.equals(TAB_PERIOD))
            setContent.setContent(R.id.tab_content_period);
        else if (tag.equals(TAB_DATE))
            setContent.setContent(R.id.tab_content_date);

        tabHost.addTab(setContent);

    }

    // Tab에 나타날 View를 구성
    private View createTabView(final Context context, String text) {
        // layoutinflater를 이용해 xml 리소스를 읽어옴
        View view = LayoutInflater.from(context).inflate(R.layout.tabwidget_date_detail, null);

        TextView tv = (TextView) view.findViewById(R.id.tabs_text);
        tv.setText(text);
        tv.setTypeface(typeface);
        return view;
    }
}