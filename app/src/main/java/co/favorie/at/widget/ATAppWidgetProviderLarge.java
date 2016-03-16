package co.favorie.at.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by quki on 2015-12-13.
 */
public class ATAppWidgetProviderLarge extends AppWidgetProvider {

    private static final String TAG = co.favorie.at.widget.ATAppWidgetProvider.class.getName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // get all ids
        ComponentName thisWidget = new ComponentName(context, ATAppWidgetProviderLarge.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // start Service
        context.startService(new Intent(context.getApplicationContext(), co.favorie.at.widget.UpdateWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        Log.i(TAG, "DELETE WIDGET");
        context.stopService(new Intent(context.getApplicationContext(), co.favorie.at.widget.UpdateWidgetService.class));
    }
}
