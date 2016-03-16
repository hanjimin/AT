package co.favorie.at.analytics;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;

import co.favorie.at.R;

/**
 * Google Analytics
 */
public class AnalyticsApplication extends SugarApp {
    private Tracker mTracker;
    public final static String GAI_CATEGORY_CHARACTER = "Character";
    public final static String GAI_CATEGORY_AT = "AT";
    public final static String GAI_CATEGORY_USER = "User";
    public final static String GAI_CATEGORY_EVENT = "Event";
    public final static String GAI_ACTION_SELECT = "Selected";
    public final static String GAI_ACTION_AT_TITLE = "TitleAT";
    public final static String GAI_ACTION_STATISTIC = "Statistic";
    public final static String GAI_ACTION_CREATE_NEWAT = "CreateNewAT";
    public final static String GAI_ACTION_AGE = "AgeString";
    public final static String GAI_LABEL_CUSTOM_DATE = "CustomDate";
    public final static String GAI_LABEL_CUSTOM_TIME = "CustomTime";


    @Override
    public void onCreate() {
        super.onCreate();

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

}
