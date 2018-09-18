package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class MainActivity extends Activity {

    private static final String TAG = "mytag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking the stored Strava API token. if no token, requesting it
        if(getStoredToken().length()!=40){
            authStrava();
        } else {
            if(internetCheck()) {
                gettingStats();
            }
        }
    }

    public void gettingStats(){

            Requester myRequester = new Requester();
            String currWeekAvg = myRequester.request(getWeekStartTimestamp(), getCurrentTimestamp(), getStoredToken());
            String prevWeekAvg = myRequester.request(String.valueOf(Integer.parseInt(getWeekStartTimestamp()) - 604800), getWeekStartTimestamp(), getStoredToken());
            TextView thisWeekTV = findViewById(R.id.tv_kms);
            TextView prevWeekTV = findViewById(R.id.tv_kms2);
            TextView txt = findViewById(R.id.textView3);
            thisWeekTV.setText(currWeekAvg);
            prevWeekTV.setText(prevWeekAvg);
            if(Float.valueOf(currWeekAvg) >= Float.valueOf(prevWeekAvg)) {
                String texxt = "This week I am " + String.format("%.1f", (100 * Float.valueOf(currWeekAvg) / Float.valueOf(prevWeekAvg) - 100)) + "% faster than previous week.";
                txt.setText(texxt);
            } else {
                String texxt = "This week I am " + String.format("%.1f", (100 * Float.valueOf(prevWeekAvg) / Float.valueOf(currWeekAvg) - 100)) + "% slower than previous week.";
                txt.setText(texxt);
            }

    }

    public void authStrava(){

        //checking for internet, if success - starting Strava Login Activity to authenticate
        if (internetCheck()){
            Intent intent = new Intent(this, StravaLoginActivity.class);
            startActivityForResult(intent, 777);
        } else {
             //will handle logic here later
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 777) {
            gettingStats();
        }
    }

    public boolean internetCheck() {
        InternetCheck check = new InternetCheck();
        boolean inet = false;
        try{
            inet = check.execute().get();
        } catch (Exception e) {
            Log.d(TAG, "authStrava: " + e.getMessage());
        }
            if(!inet){
                Toast toast = Toast.makeText(getApplicationContext(),
                    "No internet connection",  Toast.LENGTH_SHORT);
                toast.show();
            }
            return inet;
        }

    public String getStoredToken(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        return appSharedPrefs.getString("StravaToken","");
    }

    public String getWeekStartTimestamp(){ //getting the timestamp of the beginning of the week

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.AM_PM);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Long timestamp = cal.getTimeInMillis() / 1000;

        return String.valueOf(timestamp);
    }

    public String getMonthStartTimestamp(){ //getting the timestamp of the beginning of the month

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.AM_PM);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Long timestamp = cal.getTimeInMillis() / 1000;

        return String.valueOf(timestamp);
    }

    public String getCurrentTimestamp(){
        Long timestamp = System.currentTimeMillis()/1000;
        return String.valueOf(timestamp);
    }

}
