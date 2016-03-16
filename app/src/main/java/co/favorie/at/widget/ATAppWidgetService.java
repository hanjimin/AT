package co.favorie.at.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

import co.favorie.at.ATCharacterManager;
import co.favorie.at.datum.ATScheduleDatum;
import co.favorie.at.R;

/**
 * Created by bmac on 2015-08-27.
 */
public class ATAppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private ArrayList<ATScheduleDatum> data = new ArrayList<ATScheduleDatum>();
    private int mAppWidgetId, mWidgetLandWidth, mWidgetPortHeight, mWidgetPortWidth, mWidgetLandHeight;
    private DisplayMetrics screenMetrics;
    private int labelSize;
    private ATScheduleDatum t;
    private Bitmap charBitmap;
    private final static int ID_TITLE=1;
    private final static int ID_PERCENT=2;
    private final static int ID_START_DATE=3;
    private final static int ID_END_DATE=4;
    private final static int ID_LABEL=5;


    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        List<ATScheduleDatum> ATDBData;
        ATDBData = ATScheduleDatum.findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum where widget_Status order by list_Index asc");
        data.addAll(ATDBData);
    }
    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        List<ATScheduleDatum> ATDBData;
        ATDBData = SugarRecord.findWithQuery(ATScheduleDatum.class, "Select * from AT_Schedule_Datum where widget_Status order by list_Index asc");
        data.addAll(ATDBData);
    }


    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if(position < data.size()) {


            screenMetrics = mContext.getResources().getDisplayMetrics();
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_cell);
            t = data.get(position);
            t.refresh();
            rv.setInt(R.id.app_widget_cell, "setBackgroundColor", getCellColorByIndex(position));
            ATCharacterManager characterManager = ATCharacterManager.getInstance();
            characterManager.initiate(mContext);
            charBitmap = BitmapFactory.decodeResource(mContext.getResources(), characterManager.getCharacterList().get(t.selectedCharacter).widgetResourceId).copy(Bitmap.Config.ARGB_8888, true);
            buildBitmapByStr(t.lable, 32, Color.parseColor("#5a5a5a"), ID_LABEL);
            // 두번째 parameter 는 fontsize
            rv.setImageViewBitmap(R.id.widget_cell_imageview_title, buildBitmapByStr(t.title,55, Color.WHITE, ID_TITLE));
            rv.setImageViewBitmap(R.id.widget_cell_imageview_percent, buildBitmapByStr(t.percentage + "%", 55, Color.WHITE, ID_PERCENT));
            rv.setImageViewBitmap(R.id.widget_cell_imageview_start, buildBitmapByStr(t.start, 50, Color.WHITE, ID_START_DATE));
            rv.setImageViewBitmap(R.id.widget_cell_imageview_end, buildBitmapByStr(t.end, 50, Color.WHITE, ID_END_DATE));


            //bar size == screen size - 100dp
            //1percentage = bar size / 100;
            //chracter setting
            float widgetCharacterDp = 36f / (float) charBitmap.getHeight() * charBitmap.getWidth(); ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////widgetCharacterHeight!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            float screenSizeDp = screenMetrics.widthPixels / screenMetrics.density;
            float percentSizeDp = (screenSizeDp/8*6) / 100 * t.percentage;


            float leftPadding, rightPadding, leftOffset, sp = screenMetrics.scaledDensity;
            leftOffset = screenSizeDp / 8;
            leftPadding = (leftOffset + percentSizeDp);
            rightPadding = (screenSizeDp - leftPadding - widgetCharacterDp);
            labelSize = (int) (screenSizeDp - leftPadding - rightPadding);


            /*
             * progressBar
             *
             */
            int progressBitmapWidth = 500, progressBitmapHeight = 70, margin = 30;
            float  sizePerPercentage = (progressBitmapWidth - 2 * margin) / 100.0f;
            Bitmap graph = Bitmap.createBitmap(progressBitmapWidth, progressBitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(graph);
            Paint paint = new Paint();

            // 흰색(background) progressBar
            // # drawRect(float left, float top, float right, float bottom, Paint paint)
            paint.setColor(Color.WHITE);
            canvas.drawRect(margin, 55, progressBitmapWidth - margin, progressBitmapHeight, paint);

            // 빨강(graph) progressBar
            paint.setColor(Color.parseColor("#E64B55"));
            canvas.drawRect(margin, 55, margin + sizePerPercentage * t.percentage, progressBitmapHeight, paint);
            rv.setImageViewBitmap(R.id.graph, graph);


            // 캐릭터 그리기
            canvas.drawBitmap(charBitmap, null, new Rect((int)(sizePerPercentage * t.percentage), 0, 30 + (int)(sizePerPercentage * t.percentage) + 30, 60), null);
            return rv;
        } else {
            return null;
        }
    }

    public Bitmap buildBitmapByStr(String str, int size, int color, int id)
    {
        Bitmap myBitmap;
        if(id == ID_TITLE )//title
            myBitmap = Bitmap.createBitmap(1000, 90, Bitmap.Config.ARGB_8888);
        else if(id == ID_PERCENT) //percentage
            myBitmap = Bitmap.createBitmap(240, 90, Bitmap.Config.ARGB_8888);
        else if(id == ID_START_DATE || id == ID_END_DATE)//start or end
            myBitmap = Bitmap.createBitmap(150, 120, Bitmap.Config.ARGB_8888);
        else
            myBitmap = charBitmap;

        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        try {
            paint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "notosans_bold.otf"));
        } catch (Exception e) {
        }
        paint.setColor(color);

        float ratio = myBitmap.getDensity() / 480.0f;
        float fontSize;
        if(id==ID_LABEL){
            fontSize = size * ratio;
        }else{
            fontSize = size;
        }

        paint.setTextSize(fontSize);

        if(id == ID_TITLE) {
            paint.setTextAlign(Paint.Align.LEFT);
            myCanvas.drawText(str, 0, 45, paint);
        } else if(id == ID_PERCENT) {
            paint.setTextAlign(Paint.Align.RIGHT);
            myCanvas.drawText(str, 240, 45, paint);
        } else if(id == ID_START_DATE || id == ID_END_DATE) {
            if(str.contains("\n")) {
                paint.setTextAlign(Paint.Align.CENTER);
                String s1 = str.substring(0, str.indexOf("\n"));
                String s2 = str.substring(str.indexOf("\n") + 1);
                myCanvas.drawText(s1, 75, 70, paint);
                myCanvas.drawText(s2, 75, 110, paint);
            } else {
                paint.setTextAlign(Paint.Align.CENTER);
                myCanvas.drawText(str, 75, 90, paint);
            }
        } else if(id == ID_LABEL) {
            paint.setTextAlign(Paint.Align.CENTER);
            myCanvas.drawText(str, labelSize/2*screenMetrics.density + labelSize/6, 14*screenMetrics.density, paint);
        }
        return myBitmap;
    }

    public int getCellColorByIndex(int listIndex) {
        listIndex %= 14;
        switch (listIndex) {
            case 0:
                return Color.parseColor("#ABD8CC");
            case 1:case 13:
                return Color.parseColor("#C8DCCF");
            case 2:case 12:
                return Color.parseColor("#EBEBBE");
            case 3:case 11:
                return Color.parseColor("#FEE1A1");
            case 4:case 10:
                return Color.parseColor("#F7D38B");
            case 5:case 9:
                return Color.parseColor("#F4B98C");
            case 6:case 8:
                return Color.parseColor("#F29F83");
            case 7:
                return Color.parseColor("#ED8B7E");
        }
        return 0;
    }
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
