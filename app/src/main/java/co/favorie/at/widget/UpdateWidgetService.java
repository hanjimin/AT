package co.favorie.at.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import co.favorie.at.MainActivity;
import co.favorie.at.R;

/**
 * Created by bmac on 2015-10-19.
 */
public class UpdateWidgetService extends Service implements Runnable{

    private static final String TAG = UpdateWidgetService.class.getName();
    private Handler mHandler;
    private static final int UPDATE_TIME_INTERVAL = 60000; //millisecond,
    private Intent intent;
    private AppWidgetManager appWidgetManager;
    private int count;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler= new Handler();
        appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        mHandler.postDelayed(this, 1); // call run()
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"START SERVICE COMMAND");
        this.intent = intent;
        updateAppWidget();
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this); // stop run()
        stopSelf(); // stop Service
    }


    private void updateAppWidget(){

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for(int widgetId : allWidgetIds){
            Log.i(TAG, "UPDATE WIDGET");
            RemoteViews layout = buildLayout(this, widgetId);
            appWidgetManager.updateAppWidget(widgetId,layout);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.app_widget_listview);
        }

    }

    private RemoteViews buildLayout(Context context, int appWidgetId) {
        PendingIntent invokeActivity = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_layout);
        Intent svcIntent = new Intent(context, ATAppWidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(appWidgetId, R.id.app_widget_listview, svcIntent);
        rv.setEmptyView(R.id.app_widget_listview, R.id.app_widget_empty_view);
        rv.setOnClickPendingIntent(R.id.app_widget_action_bar, invokeActivity);
        return rv;
    }

    @Override
    public void run() {
        int currTime = count * UPDATE_TIME_INTERVAL/1000;
        if((currTime!=0) && (currTime%60==0))
            updateAppWidget();
        Log.i(TAG, "TIME : " + currTime);
        mHandler.postDelayed(this, UPDATE_TIME_INTERVAL);
        count++;
    }
}
