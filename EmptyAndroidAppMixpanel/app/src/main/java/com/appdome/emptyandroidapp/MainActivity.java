package com.appdome.emptyandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        // Define the LinearLayout's characteristics
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set generic layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button button = new Button(this);
        button.setText("Button");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(MainActivity.this, "Button clicked!", Toast.LENGTH_LONG).show();
                sendTrackingEvent(MainActivity.this);
            }
        });

        layout.addView(button, params); // Modify this

        setContentView(layout);
    }


    private static final String TAG = "MainActivity";
    private static final String TRACKING_TOKEN = "8f09e66cf26642ab3908f859d6a964a9";
    private static final String TRACKING_IDENTITY = "EmptyApp";


    private static void sendTrackingEvent(Context context) {

        String key = "LibloaderStartEventSent";
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        if (app_preferences.getBoolean(key, false)) {
//            // Already sent event before - not doing so again
//            return;
//        }
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putBoolean(key, true);
        editor.apply();

        Log.i(TAG, "MixPanel token: " + TRACKING_TOKEN + ", id: " + TRACKING_IDENTITY);
        try {
            Class MixpanelAPI = Class.forName("com.mixpanel.android.mpmetrics.MixpanelAPI");
            Object mixpanelAPIInstance = MixpanelAPI.getMethod("getInstance", Context.class, String.class).invoke(null, context, TRACKING_TOKEN);
            MixpanelAPI.getMethod("identify", String.class).invoke(mixpanelAPIInstance, TRACKING_IDENTITY);

            String deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            JSONObject eventDetails = new JSONObject();
            try {
                eventDetails.put("Device ID", deviceID);
                eventDetails.put("Application", context.getApplicationContext().getPackageName());
                eventDetails.put("Identity", TRACKING_IDENTITY);
            } catch (JSONException e) {
                Log.e(TAG, "Error trying to construct JSON for MixPanel first use event");
            }
            Log.i(TAG, "Sending MixPanel event of first use with deviceID: " + deviceID);
            MixpanelAPI.getMethod("track", String.class, JSONObject.class).invoke(mixpanelAPIInstance, "First Use", eventDetails);
            MixpanelAPI.getMethod("flush").invoke(mixpanelAPIInstance);
        } catch (Exception exception) {
            Log.e(TAG, "Exception while trying to call MixPanel first use event: " + exception);
            if (exception instanceof InvocationTargetException) {
                Log.e(TAG, "Exception cause: " + exception.getCause());
            }
        }
    }
}
