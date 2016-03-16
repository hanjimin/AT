package co.favorie.at.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.favorie.at.ATCharacterManager;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.MainActivity;
import co.favorie.at.R;

/**
 * Created by bmac on 2015-10-30.
 */
public class ATNotificationService extends Service implements Runnable {


    private Handler mHandler;
    private static final int UPDATE_TIME_INTERVAL = 10000; //millisecond,
    private int count;
    private NotificationManager mNotificationManager;
    private ArrayList<ATScheduleDatum> arrayListDatum = new ArrayList<>(); // Data를 list로 관리함

    // 캐릭터 부분
    private ATCharacterManager mATCharacterManager = ATCharacterManager.getInstance();
    private PendingIntent invokeActivity;

    private BroadcastReceiver screenOnBr = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // 폰이 켜지면 Service 실행
                Intent i = new Intent(context, ATNotificationService.class);
                context.startService(i);
            }
        }
    };

    // 서비스가 처음으로 생성되면 호출됩니다. 이 메소드 안에서 초기의 설정 작업을 하면되고 서비스가 이미 실행중이면 이 메소드는 호출되지 않습니다.
    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // Notification Manager
        /*Notification 클릭 시 mainactivity로 이동*/
        invokeActivity = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        /*0.001초 뒤에 run() 시작*/
        mHandler.postDelayed(this, 1); // call run(), count mode on
        /*screen이 켜지면 onStartCommand() 시작*/
        registerBR();
    }


    //다른 컴포넌트가 startService()를 호출해서 서비스가 시작되면 이 메소드가 호출됨.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null){
            try{
                registerBR();
                mHandler.postDelayed(this, 1); // call run()
            }catch (IllegalArgumentException e){
                Log.e("NotificationService",e.getMessage());
            }
        }

        /*arrayListDatum을 초기화 시키고 새로 갱신하는 작업*/
        arrayListDatum.clear();
        List<ATScheduleDatum> listTempATDatum;
        listTempATDatum = SugarRecord.findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum where noti_Status order by list_Index asc");
        arrayListDatum.addAll(listTempATDatum);
        mNotificationManager.cancelAll();
        mATCharacterManager.initiate(this);

        setATNotification(new ATScheduleDatum(), arrayListDatum);

        return START_REDELIVER_INTENT;  //메모리 공간 부족으로 서비스가 종료되었을 떄 서비스 재생성과 onStartCommand() 호출(with same intent)
    }

    // 다른 컴포넌트가 bindService()를 호출해서 서비스와 연결을 시도하면 이 메소드가 호출됩니다.
    // 이 메소드에서 IBinder를 반환해서 서비스와 컴포넌트가 통신하는데 사용하는 인터페이스를 제공해야 합니다.
    // 만약 시작 타입의 서비스를 구현한다면 null을 반환하면 됩니다.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // 서비스가 소멸되는 도중에 이 메소드가 호출되며 주로 Thread, Listener, BroadcastReceiver와 같은 자원들을 정리하는데 사용하면 됩니다.
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mHandler.removeCallbacks(this); // stop run()
            unregisterReceiver(screenOnBr);
        }catch (IllegalArgumentException e){
            Log.e("NotificationService","" + e.getMessage());
        }
    }

    @Override
    public void run() {

        int currTime = count * UPDATE_TIME_INTERVAL / 1000; //UPDATE_TIME_INTERVAL=10000
        /*60초(1분)이 지나면 Notification refresh*/
        if ((currTime % 60 == 0) && (currTime != 0)) {

            setATNotification(new ATScheduleDatum(), arrayListDatum);

        }
        /*10000 = 10초 이고 10초마다 run() 실행. 10초마다 count++*/
        mHandler.postDelayed(this, UPDATE_TIME_INTERVAL);
        count++;

    }

    /*활성화 되어 있는 Datum들을 List에서 가져와 Notification에 표시하는 함수*/
    public void setATNotification(ATScheduleDatum mATScheduleDatum, ArrayList<ATScheduleDatum> arrayListDatum) {


        if(arrayListDatum.size() != 0){
            for (int i = arrayListDatum.size() - 1; i >= 0; i--) {
                mATScheduleDatum = arrayListDatum.get(i);   //db에 있는 ATScheduleDatum 객체들 하나씩 가져온다.
                mATScheduleDatum.refresh();     //객체의 type에 따라 refesh 해주는 custom 함수들
                int diffStartEndDay= 0;
                //  1. D-DAY 기능 중(tabIndex:2) 2.오늘 이전의 날을 선택했한 경우(datePreafter:0)
                if (mATScheduleDatum.tabIndex == 2 && mATScheduleDatum.datePreafter == 0) {
                    Date startDate = mATScheduleDatum.startDate;
                    Date endDate = mATScheduleDatum.endDate;
                    diffStartEndDay = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)); //예) 100, 200, 365
                    Log.e("Test",diffStartEndDay + "");
                    // 간격이 오차가 생기는 경우에 대한 처리 예) 99.5 -> 100
                    if (diffStartEndDay % 100 != 0 && diffStartEndDay % 365 != 0) {
                        diffStartEndDay = (int) Math.ceil(diffStartEndDay + 0.1);
                    }
                }
                Bitmap charBitmap = BitmapFactory.decodeResource(getResources(),                //ATScheduleDatum에 저장된 selectedCharacter의 widgetResourceId를 가져와 Bitmap으로 formating
                        mATCharacterManager.getCharacterList().get(mATScheduleDatum.selectedCharacter).widgetResourceId).copy(Bitmap.Config.ARGB_8888, true);
                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_long120);
                buildBitmapWithStrToLabel(mATScheduleDatum.lable, 30, charBitmap, mATScheduleDatum);    //Notification에 있는 캐릭터 위에 문구가 새겨진 라벨을 달아주는 함수
                int charResId = mATCharacterManager.getCharacterList().get(arrayListDatum.get(i).selectedCharacter).detailviewResourceId;


                // Notification Builder
                Notification.Builder notificationBuilder = new Notification.Builder(this);
                notificationBuilder.setSmallIcon(charResId);
                notificationBuilder.setWhen(System.currentTimeMillis());
                notificationBuilder.setContentIntent(invokeActivity);
                notificationBuilder.setAutoCancel(false);
                RemoteViews mRemoteView;
                // RemoteView 커스터마이징
                if (mATScheduleDatum.tabIndex == 2 && mATScheduleDatum.datePreafter == 0) {
                    mRemoteView = new RemoteViews(getPackageName(), R.layout.at_notification_cell_extensible);
                }
                else {
                    mRemoteView = new RemoteViews(getPackageName(), R.layout.at_notification_cell);
                }
                mRemoteView.setTextViewText(R.id.noti_title, "" + mATScheduleDatum.title);
                mRemoteView.setTextViewText(R.id.noti_percentage, mATScheduleDatum.percentage + "%");
                mRemoteView.setTextViewText(R.id.noti_start, "" + mATScheduleDatum.start);
//                mRemoteView.setTextViewText(R.id.noti_end, "" + mATScheduleDatum.end);

                //  1. D-DAY 기능 중(tabIndex:2) 2.오늘 이전의 날을 선택했한 경우(datePreafter:0)
                if (mATScheduleDatum.tabIndex == 2 && mATScheduleDatum.datePreafter == 0){

                    if(diffStartEndDay % 365 == 0){
                        mRemoteView.setTextViewText(R.id.noti_mid, diffStartEndDay / 365 + "y");
                        int endDay=  (int)(Math.ceil((double)diffStartEndDay/100)*100);  // ex) 365 -> 400
                        mRemoteView.setTextViewText(R.id.noti_end, endDay + "");
                    }else{
                        int n = diffStartEndDay/365+1;
                        mRemoteView.setTextViewText(R.id.noti_mid, String.valueOf(diffStartEndDay));

                        //    300          1y
                        // |||||||||||||||||||
                        // 위와 같이 400이 아닌 1y로 처리해주기 위함.
                        if(diffStartEndDay<365*n && 365*n < diffStartEndDay+100){
                            mRemoteView.setTextViewText(R.id.noti_end, n+"y");
                        }else{
                            mRemoteView.setTextViewText(R.id.noti_end, diffStartEndDay + 100 + "");
                        }
                    }
                }
                else if(mATScheduleDatum.type == 2){
                    mRemoteView.setTextViewText(R.id.noti_start, "" + mATScheduleDatum.start + "\n" + mATScheduleDatum.unit);
                    mRemoteView.setTextViewText(R.id.noti_end, "" + mATScheduleDatum.end + "\n" + mATScheduleDatum.unit);
                }

                else{
                    mRemoteView.setTextViewText(R.id.noti_end, mATScheduleDatum.end);
                }
                /////////////////////////////////////////////////////////////////////////
                /////////   Rect(x,y,width,height)                           ////////////
                /////////   drawRect(left,top,right,bottom)                  ////////////
                /////////////////////////////////////////////////////////////////////////

            /*
             * progressBar
             *
             */
                int progressBitmapWidth = 500, progressBitmapHeight = 70, left = 30, top = 55;
                float sizePerPercentage = (progressBitmapWidth - 2 * left) / 100.0f;
                Bitmap graph = Bitmap.createBitmap(progressBitmapWidth, progressBitmapHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(graph);
                Paint paint = new Paint();

                // 흰색(background) progressBar
                // # drawRect(float left, float top, float right, float bottom, Paint paint)
                if (mATScheduleDatum.tabIndex == 2 && mATScheduleDatum.datePreafter == 0) {
                    int w = image.getWidth();
                    int h = image.getHeight();
                    Rect src = new Rect(0, 0, w, h);
                    Log.e("DD?", h + ""+w);
                    Rect dst = new Rect(350, 55, 470, 70);
                    canvas.drawBitmap(image, src, dst, null);

                    paint.setColor(Color.WHITE);
                    canvas.drawRect(left, top, (int) ((progressBitmapWidth - left) * 0.8), progressBitmapHeight, paint);
                    // 빨강(graph) progressBar
                    paint.setColor(Color.parseColor("#E64B55"));
                    canvas.drawRect(left, top, (int) ((left + (int) (sizePerPercentage * mATScheduleDatum.percentage)) * 0.8), progressBitmapHeight, paint);
                    // 캐릭터 그리기
                    canvas.drawBitmap(charBitmap, null, new Rect((int) (sizePerPercentage * mATScheduleDatum.percentage * 0.8), 0, (int) ((30 + (int) (sizePerPercentage * mATScheduleDatum.percentage) * 0.8)+25), 60), null);

                }
                else{
                    paint.setColor(Color.WHITE);
                    canvas.drawRect(left, top, progressBitmapWidth - left, progressBitmapHeight, paint);
                    // 빨강(graph) progressBar
                    paint.setColor(Color.parseColor("#E64B55"));
                    canvas.drawRect(left, top, left + (int) (sizePerPercentage * mATScheduleDatum.percentage), progressBitmapHeight, paint);
                    // 캐릭터 그리기
                    canvas.drawBitmap(charBitmap, null, new Rect((int) (sizePerPercentage * mATScheduleDatum.percentage), 0, 30 + (int) (sizePerPercentage * mATScheduleDatum.percentage) + 30, 60), null);
                }

                mRemoteView.setImageViewBitmap(R.id.noti_graph, graph);
                Notification noti = notificationBuilder.build();
                noti.flags = Notification.FLAG_NO_CLEAR;
                noti.priority = Notification.PRIORITY_MAX;
                noti.contentView = mRemoteView;


                if(i==(arrayListDatum.size()-1)){
                    // 최소 하나의 noti는 foreground 에서 실행된다.
                    startForeground(i + 1, noti);
                }else{
                    mNotificationManager.notify(i, noti);
                }
            }
        }else{
            stopForeground(true);
            stopSelf(); // stop Service
        }

    }

    // 캐릭터, Label에 Text집어 넣어 그리는 함수
    public void buildBitmapWithStrToLabel(String labelText, int size, Bitmap myBitmap, ATScheduleDatum mATScheduleDatum) {

        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setColor(Color.parseColor("#5a5a5a"));
        try {
            paint.setTypeface(Typeface.createFromAsset(getAssets(), "notosans_bold.otf"));
        } catch (Exception e) {
        }
        float ratio = myBitmap.getDensity() / 480f;
        float fontSize;
        fontSize = size * ratio;
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        DisplayMetrics screenMetrics = getResources().getDisplayMetrics();
        //bar size == screen size - 100dp
        //1percentage = bar size / 100;
        //chracter setting
        float widgetCharacterDp = 36f / (float) myBitmap.getHeight() * myBitmap.getWidth(); ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////widgetCharacterHeight!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        float screenSizeDp = screenMetrics.widthPixels / screenMetrics.density;
        float percentSizeDp = (screenSizeDp / 8 * 6) / 100 * mATScheduleDatum.percentage;
        float leftPadding, rightPadding, leftOffset;
        leftOffset = screenSizeDp / 8;
        leftPadding = (leftOffset + percentSizeDp);
        rightPadding = (screenSizeDp - leftPadding - widgetCharacterDp);
        int labelSize = (int) (screenSizeDp - leftPadding - rightPadding);
        myCanvas.drawText(labelText, labelSize / 2 * screenMetrics.density + labelSize / 6, 14 * screenMetrics.density, paint);

    }

    private void registerBR() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnBr, filter);
    }


}
