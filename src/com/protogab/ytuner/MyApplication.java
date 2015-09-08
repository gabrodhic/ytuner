package com.protogab.ytuner;

import java.util.HashMap;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.GoogleAnalytics;

@ReportsCrashes(
    formKey = "", // This is required for backward compatibility but not used
    formUri = "http://protogab.com/ytuner/tuner/report",
    forceCloseDialogAfterToast = false, // optional, default false
    resToastText = R.string.crash_toast_text,
    additionalSharedPreferences={"pref*","pref_*"}
)
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        if (!BuildConfig.DEBUG) ACRA.init(this);//Only init ACRA if NOT on DEBUG MODE
    }
    

 // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID =  "UA-48097455-2"; //R.string.ga_trackingId

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(
                            R.xml.global_tracker)
                            : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
    
}