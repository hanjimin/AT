package co.favorie.at;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.favorie.at.analytics.AnalyticsApplication;
import co.favorie.at.customview.AnimatingSeekBar;
import co.favorie.at.datum.ATDatumForTransmit;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.notification.ATNotificationService;
import co.favorie.at.preference.AlarmDialogSession;
import co.favorie.at.widget.ATAppWidgetProvider;
import co.favorie.at.widget.ATAppWidgetProviderLarge;

import static co.favorie.at.analytics.AnalyticsApplication.GAI_ACTION_AGE;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_ACTION_AT_TITLE;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_ACTION_CREATE_NEWAT;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_CATEGORY_EVENT;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_CATEGORY_USER;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_LABEL_CUSTOM_DATE;
import static co.favorie.at.analytics.AnalyticsApplication.GAI_LABEL_CUSTOM_TIME;
import static co.favorie.at.datum.ATScheduleDatum.CUSTOM;
import static co.favorie.at.datum.ATScheduleDatum.DAILY;
import static co.favorie.at.datum.ATScheduleDatum.DATE_TO_DATE;
import static co.favorie.at.datum.ATScheduleDatum.LIFETIME;
import static co.favorie.at.datum.ATScheduleDatum.MONTHLY;
import static co.favorie.at.datum.ATScheduleDatum.TIME_TO_TIME;
import static co.favorie.at.datum.ATScheduleDatum.WEEKLY;
import static co.favorie.at.datum.ATScheduleDatum.WEEKLY_MON;
import static co.favorie.at.datum.ATScheduleDatum.YEARLY;
import static co.favorie.at.datum.ATScheduleDatum.findWithQuery;
import static co.favorie.at.datum.ATScheduleDatum.saveInTx;



public class MainActivity extends Activity {

    ATDatumForTransmit atDatumForTransmit = ATDatumForTransmit.getInstance();
    Context context = this;
    ArrayList<ATScheduleDatum> ATDataList = new ArrayList<>();
    MainListAdapter mainListAdapter;
    DynamicListView mainListView;
    ATCharacterManager characterManager;
    LinearLayout defaultCharacter;
    int selectedPosition = 0;

    final static int ACTIVITY_FOR_INSERT = 1;
    final static int ACTIVITY_FOR_UPDATE = 2;
    final static int ACTIVITY_FROM_WIDGET = 3;

    public static Typeface BLACK_NOTO = null;
    public static Typeface BOLD_NOTO = null;

    final static int TOGGLE_DEFAULT = 0;
    final static int TOGGLE_PLUS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (MainActivity.BLACK_NOTO == null)
            MainActivity.BLACK_NOTO = Typeface.createFromAsset(getAssets(), "notosans_black.otf");
        if (MainActivity.BOLD_NOTO == null)
            MainActivity.BOLD_NOTO = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상태바 service 시작
        startService(new Intent(getApplicationContext(), ATNotificationService.class));

        characterManager = ATCharacterManager.getInstance();
        characterManager.initiate(context);

        View customActionBar = (View) findViewById(R.id.main_activity_action_bar);
        Button rightButton = (Button) customActionBar.findViewById(R.id.at_action_bar_button_right);
        //////////////////////////////
        /*  right button to insert  */
        //////////////////////////////
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SelectActivity.class);
                startActivityForResult(i, ACTIVITY_FOR_INSERT);
            }
        });
        defaultCharacter = (LinearLayout) findViewById(R.id.default_character_view);

        mainListView = (DynamicListView) findViewById(R.id.main_activity_listview);
        mainListView.setDivider(null);

        /////////////////////////////////////////////////////////////////////

//        ATScheduleDatum.executeQuery("DROP TABLE AT_SCHEDULE_DATUM");
        /*등록된 ATScheduleDatum이 하나도 없을 때 어플리케이션 재시작 시 다섯가지의 기본 Datum이 자동 생성됨*/
        List<ATScheduleDatum> ATDBData;
        ATDBData = findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum order by list_Index asc");
        if (ATDBData.size() == 0) {
            ATScheduleDatum ATDaily = new ATScheduleDatum();
            ATDaily.set(getString(R.string.default_daily), new Date(), new Date(), DAILY, 0, true, true);
            ATDaily.listIndex = 0;
            ATScheduleDatum ATWeekly = new ATScheduleDatum();
            ATWeekly.set(getString(R.string.default_weekly), new Date(), new Date(), WEEKLY, 0, true, false);
            ATWeekly.listIndex = 1;
            ATScheduleDatum ATMonthly = new ATScheduleDatum();
            ATMonthly.set(getString(R.string.default_monthly), new Date(), new Date(), MONTHLY, 0, false, false);
            ATMonthly.listIndex = 2;
            ATScheduleDatum ATYearly = new ATScheduleDatum();
            ATYearly.set(getString(R.string.default_yearly), new Date(), new Date(), YEARLY, 0, false, false);
            ATYearly.listIndex = 3;
            ATScheduleDatum ATLifetime = new ATScheduleDatum();
            ATLifetime.set(getString(R.string.default_life_time), new Date(), new Date(), LIFETIME, 0, false, false);
            ATLifetime.listIndex = 4;

            ATDBData.add(ATDaily);
            ATDBData.add(ATWeekly);
            ATDBData.add(ATMonthly);
            ATDBData.add(ATYearly);
            ATDBData.add(ATLifetime);

            saveInTx(ATDBData);////////////////////DB Initiated

        }
        //////////////////////////////////////////////////////////////////////////////////


        /*list의 item을 swipe 했을 때의 event와 Drag and Drop 했을 때의 event 처리*/
        ATDataList.addAll(ATDBData);
        Log.e("++ATDBData++", ATDBData + "");
        mainListAdapter = new MainListAdapter(context, ATDataList);
        TimedUndoAdapter undoAdapter = new TimedUndoAdapter(mainListAdapter, this, new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) { //when swiped twice!!!!!!!!!!!!!!!!
                //mainListView.disableDragAndDrop();
                for (int pos : ints) {
                    mainListAdapter.remove(pos);
                    ATScheduleDatum t = ATDataList.get(pos);
                    List<ATScheduleDatum> data = findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum where list_Index > ?", String.valueOf(pos));
                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).listIndex--;
                        data.get(i).save();
                    }
                    t.delete();
                    ATDataList.remove(pos);
                    setDefaultCharacter(ATDataList, defaultCharacter);
                    startService(new Intent(getApplicationContext(), ATNotificationService.class));
                }
            }
        });
        undoAdapter.setTimeoutMs(1000 * 60 * 60 * 24);
        undoAdapter.setAbsListView(mainListView);
        mainListView.setAdapter(undoAdapter);
//        mainListView.enableSimpleSwipeUndo();
        mainListView.enableDragAndDrop();
        mainListView.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                int originalPos = i, newPos = i1;
                ATScheduleDatum ti;
                ti = ATDataList.get(originalPos);
                ATDataList.remove(originalPos);
                ti.listIndex = newPos;
                ATDataList.add(newPos, ti);
                ti.save();
                if (i > i1) { // original pos > new pos // go upside
                    for (int j = newPos + 1; j <= originalPos; j++) {
                        ATDataList.get(j).listIndex++;
                        ATDataList.get(j).save();
                    }
                } else { // original pos < new pos //go downside
                    for (int j = originalPos; j < newPos; j++) {
                        ATDataList.get(j).listIndex--;
                        ATDataList.get(j).save();
                    }
                }
                mainListAdapter.setATData(ATDataList);
            }
        });
        mainListView.setLongClickable(true);
        mainListView.setClickable(true);

    }

    /*main page에 들어 왔을 떄 새로운 신규 캐릭터가 추가 되었을 시 preference에 저장되어 있던 이전 캐릭터의 총 수와 현재 캐릭터의 총 수를 비교해 변화가 없으면
    * alert 알림 창을 생성하지 않고 신규 캐릭터가 추가되어 수가 다를 경우 alert 알림 창을 생성해 새로운 캐릭터의 정보를 보여준다.*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int charListSize = characterManager.getCharacterList().size();
        int charListLastIndex = characterManager.getCharacterList().size() - 1;
        AlarmDialogSession mAlarmDialogSession = new AlarmDialogSession(getApplicationContext());
        if (mAlarmDialogSession.isAddedNewCharacter(charListSize)) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alarm_alertdialog, null);
            alertBuilder.setView(dialogView).setCancelable(true);
            Button alarmOkBtn = (Button) dialogView.findViewById(R.id.alarm_ok);
            alarmOkBtn.setTypeface(MainActivity.BOLD_NOTO);
            TextView alarmText = (TextView) dialogView.findViewById(R.id.alarm_text);
            alarmText.setTypeface(MainActivity.BOLD_NOTO);
            ImageView alarmImageCharacter = (ImageView) dialogView.findViewById(R.id.alarm_imageCharacter);
            ImageView alarmImageCharacterNext = (ImageView) dialogView.findViewById(R.id.alarm_imageCharacter_next);

            if (characterManager.getCharacterList().get(charListLastIndex).isPackage) {

                // 패키지 상품일 때..
                alarmImageCharacter.setImageResource(characterManager.getCharacterList().get((charListLastIndex - 1)).detailviewResourceId);
                alarmImageCharacterNext.setImageResource(characterManager.getCharacterList().get(charListLastIndex).detailviewResourceId);
                alarmImageCharacterNext.setVisibility(View.VISIBLE);
                alarmText.setText(getString(R.string.alarm_top_one_plus_one));

            } else {
                alarmImageCharacter.setImageResource(characterManager.getCharacterList().get(charListLastIndex).detailviewResourceId);
                alarmImageCharacterNext.setVisibility(View.GONE);
                alarmText.setText(getString(R.string.alarm_top_free_events));
            }

            final AlertDialog mAlertDialog = alertBuilder.create();


            alarmOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mAlertDialog.cancel();
                    /////////////////////////////////////////////////////////////////

                }
            });
            mAlertDialog.show();
        } else {
            // 두번째 입장
            Log.d("no new character", "새로 추가된 캐릭터 없음");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 위젯 부분 !!!!!!!!!!!!!!!!!!
        // sendToWidgetProvider();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainListAdapter.notifyDataSetChanged();     //list 갱신
        setDefaultCharacter(ATDataList, defaultCharacter);  //Datum이 없으면 defaultCharacter 보이게 한다.
        AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
        Tracker mTracker = analyticsApplication.getDefaultTracker();
        mTracker.setScreenName("Main Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // requesting WidgetManager by Provider
    private void sendToWidgetProvider() {
        Intent intent = new Intent(this, ATAppWidgetProvider.class);
        Intent intentSmall = new Intent(this, ATAppWidgetProviderLarge.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intentSmall.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ATAppWidgetProvider.class));
        int idsSmall[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ATAppWidgetProviderLarge.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intentSmall.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsSmall);
        sendBroadcast(intent);
        sendBroadcast(intentSmall);
    }

    /*
    startActivityForResult를 호출한 activity의  event의 처리 후 resultCode에 따라 상황을 판단해 다시 MainActivity로 돌아왔을 떄 처리해야 할 event들 정의
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            atDatumForTransmit = co.favorie.at.datum.ATDatumForTransmit.getInstance();
            AnalyticsApplication analyticsApplication = (AnalyticsApplication) getApplication();
            Tracker mTracker = analyticsApplication.getDefaultTracker();
            if (requestCode == ACTIVITY_FOR_INSERT) {       //MainActivity에서 우측 상단의 insert 버튼을 눌러 작업을 마치고 다시 돌아왔을 때의 처리
                if (atDatumForTransmit.completed) {         //Datum이 추가 되었을 때
                    atDatumForTransmit.atScheduleDatum.refresh(); // must do refresh() to avoid null point exception - String start, end, ...
                    atDatumForTransmit.atScheduleDatum.listIndex = ATDataList.size();
                    ATDataList.add(atDatumForTransmit.atScheduleDatum);
                    atDatumForTransmit.atScheduleDatum.save(); ////////////////////DB Insert Datum by DateDetailActivity
                    mainListAdapter.setATData(ATDataList);

                    switch (atDatumForTransmit.atScheduleDatum.type) {
                        case DATE_TO_DATE:
                            mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_CREATE_NEWAT).setLabel(GAI_LABEL_CUSTOM_DATE).build());
                            mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_AT_TITLE).setLabel(atDatumForTransmit.atScheduleDatum.title).build());
                            break;
                        case TIME_TO_TIME:
                            mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_CREATE_NEWAT).setLabel(GAI_LABEL_CUSTOM_TIME).build());
                            mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_AT_TITLE).setLabel(atDatumForTransmit.atScheduleDatum.title).build());
                            break;
                    }
                }
            } else if (requestCode == ACTIVITY_FOR_UPDATE) {        //MainActivity에서 list에 있는 item중 하나의 Datum을 눌러 작업을 마치고 다시 돌아왔을 떄의 처리
                if (data.getIntExtra("update_or_remove", 0) == 0) {
                    if (atDatumForTransmit.completed) {         //변화가 있을 때의 처리 -> update 작업
                        ATDataList.get(selectedPosition).delete();
                        ATDataList.remove(selectedPosition);
                        ATDataList.add(selectedPosition, atDatumForTransmit.atScheduleDatum);
                        atDatumForTransmit.atScheduleDatum.save();
                        mainListAdapter.setATData(ATDataList);
                        switch (atDatumForTransmit.atScheduleDatum.type) {
                            case DATE_TO_DATE:
                                mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_AT_TITLE).setLabel(atDatumForTransmit.atScheduleDatum.title).build());
                                break;
                            case TIME_TO_TIME:
                                mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_EVENT).setAction(GAI_ACTION_AT_TITLE).setLabel(atDatumForTransmit.atScheduleDatum.title).build());
                                break;
                            case DAILY:
                            case WEEKLY:
                            case WEEKLY_MON:
                            case MONTHLY:
                            case YEARLY:
                                break;
                            case LIFETIME:
                                mTracker.send(new HitBuilders.EventBuilder().setCategory(GAI_CATEGORY_USER).setAction(GAI_ACTION_AGE).setLabel("User Age").setValue(atDatumForTransmit.atScheduleDatum.additionalDatum).build());
                                break;
                        }
                    }
                } else {
                    mainListAdapter.remove(selectedPosition);
                    ATScheduleDatum t = ATDataList.get(selectedPosition);
                    List<ATScheduleDatum> datum_data = findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum where list_Index > ?", String.valueOf(selectedPosition));
                    for (int i = 0; i < datum_data.size(); i++) {
                        datum_data.get(i).listIndex--;
                        datum_data.get(i).save();
                    }
                    t.delete();
                    ATDataList.remove(selectedPosition);
                    setDefaultCharacter(ATDataList, defaultCharacter);
                    startService(new Intent(getApplicationContext(), ATNotificationService.class));
                }

            }

            /*위젯에도 변경사항 알려준다.*/
            /*Intent intent = new Intent(this, ATAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ATAppWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);*/
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private class MainListAdapter extends ArrayAdapter<ATScheduleDatum> implements UndoAdapter {
        Context mContext;

        public MainListAdapter(Context context, ArrayList<ATScheduleDatum> items) {
            mContext = context;
            addAll(items);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        /*list item의 구성 조작*/
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int position_temp = position;
            View v = convertView;
            final ATScheduleDatum datum = getItem(position);
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int diffStartEndDay = 0;

            //  1. D-DAY 기능 중(tabIndex:2) 2.오늘 이전의 날을 선택했한 경우(datePreafter:0)
            if (datum.tabIndex == 2 && datum.datePreafter == 0) {
                v = vi.inflate(R.layout.at_listview_cell_extensible, null);

                diffStartEndDay = getDuration(datum);


            } else {
                v = vi.inflate(R.layout.at_listview_cell, null);
            }

            datum.refresh();
            if (datum != null) {
                final AnimatingSeekBar bar = (AnimatingSeekBar) v.findViewById(R.id.cell_seekbar);
                TextView title = (TextView) v.findViewById(R.id.cell_textview_title);
                TextView percent = (TextView) v.findViewById(R.id.cell_textview_percent);
                final TextView start = (TextView) v.findViewById(R.id.cell_textview_start);
                TextView mid = (TextView) v.findViewById(R.id.cell_textview_mid);
                final TextView end = (TextView) v.findViewById(R.id.cell_textview_end);
                ImageView dot = (ImageView) v.findViewById(R.id.cell_imageview_dot);
                ImageButton btn_thumb = (ImageButton) v.findViewById(R.id.imgbtn_thumb);

                final TextView txt_label = (TextView) v.findViewById(R.id.txt_label);
                bar.setMax(100);
                bar.startProgressAnimation(datum.percentage);
                bar.setThumbDrawableWithString(characterManager.characterList.get(datum.selectedCharacter).listviewResourceId, datum.lable);
                bar.setThumbButton(btn_thumb);
                bar.setThumbLabel(txt_label);
                bar.setEnabled(false);
                //   bar.setFocusable(false);
                //    bar.setClickable(false);
                title.setText(datum.title);
                percent.setText(datum.percentage + "%");
                start.setText(datum.start);

                //  1. D-DAY 기능 중(tabIndex:2) 2.오늘 이전의 날을 선택했한 경우(datePreafter:0)
                if (datum.tabIndex == 2 && datum.datePreafter == 0) {

                    if (diffStartEndDay % 365 == 0) {
                        mid.setText(diffStartEndDay / 365 + "y");  // ex) 365 -> 1y
                        int endDay = (int) (Math.ceil((double) diffStartEndDay / 100) * 100);  // ex) 365 -> 400
                        end.setText(endDay + "");
                    } else {
                        int n = diffStartEndDay / 365 + 1;
                        mid.setText(String.valueOf(diffStartEndDay));

                        //    300          1y
                        // |||||||||||||||||||
                        // 위와 같이 400이 아닌 1y로 처리해주기 위함.
                        if (diffStartEndDay < 365 * n && 365 * n < diffStartEndDay + 100) {
                            end.setText(n + "y");
                        } else {
                            end.setText(diffStartEndDay + 100 + "");
                        }
                    }
                }
                else if(datum.type == 2){
                    RelativeLayout.LayoutParams startChangeMargin = (RelativeLayout.LayoutParams) start.getLayoutParams();
                    RelativeLayout.LayoutParams endChangeMargin = (RelativeLayout.LayoutParams) end.getLayoutParams();

                    startChangeMargin.bottomMargin = 30;
                    endChangeMargin.bottomMargin = 30;
                    start.setLayoutParams(startChangeMargin);
                    end.setLayoutParams(endChangeMargin);

                    start.setText(datum.start + "\n"+ datum.unit);
                    end.setText(datum.end + "\n"+ datum.unit);
                }
                else {
                    end.setText(datum.end);
                }

                if (!datum.notiStatus)
                    dot.setVisibility(View.INVISIBLE);
                else
                    dot.setVisibility(View.VISIBLE);

                v.setBackgroundColor(getCellColorByIndex(position));

                v.setClickable(true);
                v.setLongClickable(true);

                btn_thumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String lableStr = datum.lable;
                        if (!lableStr.equals("Zzz") && !lableStr.equals("D-Day")) {
                            // 날짜에서 '일' 선택했을 때, 오늘 보다 이전 날짜를 선택한 경우
                            if (datum.tabIndex == 2 && datum.datePreafter == 0) {
                                // DB LABEL type 은 String 이다. 양수는 +가 붙어있는데 int 형으로 바꾸기 위해 +를 제거하는 작업

                                if (lableStr.contains("+")) {
                                    lableStr = lableStr.substring(1);
                                }

                                int mLabel = Integer.parseInt(lableStr);
                                switch (datum.toggleIndex) {

                                    // 음수 -> 양수
                                    case TOGGLE_DEFAULT: {
                                        datum.set(TOGGLE_PLUS, mLabel + getDuration(datum) + 1);
                                        atDatumForTransmit.atScheduleDatum = datum;
                                        atDatumForTransmit.completed = true;
                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화
                                        break;
                                    }

                                    // 양수 -> 음수
                                    case TOGGLE_PLUS: {
                                        datum.set(TOGGLE_DEFAULT, mLabel - getDuration(datum) - 1);
                                        atDatumForTransmit.atScheduleDatum = datum;
                                        atDatumForTransmit.completed = true;
                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화
                                        break;
                                    }
                                }

                                // 날짜에서 '기간' 선택했을 때
                            } else if (datum.tabIndex == 1) {

                                // DB LABEL type 은 String 이다. 양수는 +가 붙어있는데 int 형으로 바꾸기 위해 +를 제거하는 작업
                                if (lableStr.contains("+")) {
                                    lableStr = lableStr.substring(1);
                                }

                                int mLabel = Integer.parseInt(lableStr);
                                switch (datum.toggleIndex) {

                                    // 음수 -> 양수
                                    case TOGGLE_DEFAULT: {
                                        datum.set(TOGGLE_PLUS, mLabel + getDuration(datum));
                                        atDatumForTransmit.atScheduleDatum = datum;
                                        atDatumForTransmit.completed = true;
                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화
                                        break;
                                    }

                                    // 양수 -> 음수
                                    case TOGGLE_PLUS: {
                                        datum.set(TOGGLE_DEFAULT, mLabel - getDuration(datum));
                                        atDatumForTransmit.atScheduleDatum = datum;
                                        atDatumForTransmit.completed = true;
                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화*/
                                        break;
                                    }
                                }
                            }

//                            if (datum.type == WEEKLY || datum.type == WEEKLY_MON) {
//                                switch (datum.toggleIndex) {
//                                    // 일 -> 월
//                                    case TOGGLE_DEFAULT: {
//                                        datum.set(TOGGLE_PLUS);
//                                        atDatumForTransmit.atScheduleDatum = datum;
//                                        atDatumForTransmit.completed = true;
//                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화*/
//                                        start.setText("월");
//                                        end.setText("일");
//                                        Log.e("ddd ", "오냐?????????");
//                                    }
//                                    // 월 -> 일
//                                    case TOGGLE_PLUS: {
//                                        datum.set(TOGGLE_DEFAULT);
//                                        atDatumForTransmit.atScheduleDatum = datum;
//                                        atDatumForTransmit.completed = true;
//                                        atDatumForTransmit.atScheduleDatum.save(); // DB 초기화*/
//                                        start.setText("일");
//                                        end.setText("토");
//                                    }
//                                }
//                            }
                            bar.setThumbDrawableWithString(characterManager.characterList.get(datum.selectedCharacter).listviewResourceId, datum.lable);
                            bar.setThumbLabel(txt_label);
                        }
                    }
                });

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        co.favorie.at.datum.ATDatumForTransmit datum = co.favorie.at.datum.ATDatumForTransmit.getInstance();
                        datum.atScheduleDatum = ATDataList.get(position);
                        datum.completed = true;
                        int selected_char_position = datum.atScheduleDatum.selectedCharacter;
                        selectedPosition = position;
                        Intent i;
                        switch (datum.atScheduleDatum.type) {
                            case DATE_TO_DATE:
                                i = new Intent(MainActivity.this, DateDetailActivity.class);
                                i.putExtra("isUpdate", true);
                                i.putExtra("colorCode", mainListAdapter.getCellColorByIndex(position));
                                i.putExtra("update_or_insert", 0); // update = 0, insert = 0 update 화면 일 시 지우기 버튼이 안보이게 하기 위해 구분
                                i.putExtra("selected_char_position",selected_char_position);
                                startActivityForResult(i, ACTIVITY_FOR_UPDATE);
                                break;
                            case TIME_TO_TIME:
                                i = new Intent(MainActivity.this, TimeDetailActivity.class);
                                i.putExtra("isUpdate", true);
                                i.putExtra("colorCode", mainListAdapter.getCellColorByIndex(position));
                                i.putExtra("selected_char_position",selected_char_position);
                                startActivityForResult(i, ACTIVITY_FOR_UPDATE);
                                break;
                            case CUSTOM:
                                i = new Intent(MainActivity.this, CustomDetailActivity.class);
                                i.putExtra("isUpdate", true);
                                i.putExtra("colorCode", mainListAdapter.getCellColorByIndex(position));
                                i.putExtra("selected_char_position",selected_char_position);
                                startActivityForResult(i, ACTIVITY_FOR_UPDATE);
                                break;
                            case DAILY:
                            case WEEKLY:
                            case WEEKLY_MON:
                            case MONTHLY:
                            case YEARLY:
                            case LIFETIME:
                                i = new Intent(MainActivity.this, UpdateDefaultATActivity.class);
                                i.putExtra("isUpdate", true);
                                i.putExtra("selected_char_position",selected_char_position);
                                if (datum.atScheduleDatum.type == LIFETIME) {
                                    i.putExtra("isLifeTime", true);
                                } else if (datum.atScheduleDatum.type == WEEKLY || datum.atScheduleDatum.type == WEEKLY_MON) {
                                    i.putExtra("isWeekly", true);
                                }
                                i.putExtra("colorCode", mainListAdapter.getCellColorByIndex(position));
                                startActivityForResult(i, ACTIVITY_FOR_UPDATE);
                        }
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        System.out.println("position : " + position);
                        mainListView.startDragging(position);
                        System.out.println("you did long click " + position);
                        return true;
                    }
                });

            }
            return v;

        }

        /**
         * mid 에 들어가는 값으로써 시작날과 끝나는날의 간격임.
         *
         * @param datum
         * @return
         */
        private int getDuration(ATScheduleDatum datum) {
            Date startDate = datum.startDate;
            Date endDate = datum.endDate;
            int diffStartEndDay = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)); //예) 100, 200, 365

            // 간격이 오차가 생기는 경우에 대한 처리 예) 99.5 -> 100
            if (diffStartEndDay % 100 != 0 && diffStartEndDay % 365 != 0) {
                diffStartEndDay = (int) Math.ceil(diffStartEndDay + 0.1);
            }
            return diffStartEndDay;
        }

        public void setATData(ArrayList<ATScheduleDatum> t) {
            clear();
            addAll(t);
            notifyDataSetChanged();
            startService(new Intent(getApplicationContext(), ATNotificationService.class));
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        /*list item 왼쪽으로 drag 했을 떄 삭제 또는 취소 event 발생*/
        @NonNull
        @Override
        public View getUndoView(int i, View view, @NonNull ViewGroup viewGroup) {
            //mainListView.disableDragAndDrop();
            View v = view;
            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.undo_row, viewGroup, false);
            }
            final int p = i;
            Button removeButton = (Button) v.findViewById(R.id.undo_row_left_button);
            removeButton.setTypeface(MainActivity.BOLD_NOTO);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainListView.fling(p);
                }
            });
            return v;
        }

        @NonNull
        @Override
        public View getUndoClickView(@NonNull View view) {

            Button cancelButton = (Button) view.findViewById(R.id.undo_row_right_button);
            cancelButton.setTypeface(MainActivity.BOLD_NOTO);

            return cancelButton;
        }

        /*list의 item의 위치에 따라 배경색 지정*/
        public int getCellColorByIndex(int listIndex) {
            listIndex %= 14;
            switch (listIndex) {
                case 0:
                    return Color.parseColor("#ABD8CC");
                case 1:
                case 13:
                    return Color.parseColor("#C8DCCF");
                case 2:
                case 12:
                    return Color.parseColor("#EBEBBE");
                case 3:
                case 11:
                    return Color.parseColor("#FEE1A1");
                case 4:
                case 10:
                    return Color.parseColor("#F7D38B");
                case 5:
                case 9:
                    return Color.parseColor("#F4B98C");
                case 6:
                case 8:
                    return Color.parseColor("#F29F83");
                case 7:
                    return Color.parseColor("#ED8B7E");
            }
            return 0;
        }
    }

    private void setDefaultCharacter(ArrayList<ATScheduleDatum> ATDatumList, LinearLayout defaultCharacter) {

        if (ATDatumList != null) {
            if (ATDatumList.size() == 0) {
                defaultCharacter.setVisibility(View.VISIBLE);
            } else {
                defaultCharacter.setVisibility(View.GONE);
            }
        }
    }

}
