package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {

    private static final String TAG = "mytag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");

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
        Log.d(TAG, "gettingStats: ");
        ArrayList<Block> blocks = new ArrayList<>();
        Requester myRequester = new Requester();

        float thisWeekAvg = myRequester.request(getWeekStartTimestamp(), getCurrentTimestamp(), getStoredToken());
        float prevWeekAvg = myRequester.request(String.valueOf(Integer.parseInt(getWeekStartTimestamp()) - 604800), getWeekStartTimestamp(), getStoredToken());
        Block week = new Block();
        week.setPercent(thisWeekAvg/prevWeekAvg);
        week.setTime("week");

        blocks.add(week);

        float thisMonthAvg = myRequester.request(getMonthStartTimestamp(), getCurrentTimestamp(), getStoredToken());
        float prevMonthAvg = myRequester.request(getPrevMonthStartTimestamp(), getMonthStartTimestamp(), getStoredToken());
        //Log.d(TAG, "thMavg=" + thisMonthAvg + " prMavg=" + prevMonthAvg);
        Block month = new Block();
        month.setPercent(thisMonthAvg/prevMonthAvg);
        month.setTime("month");

        blocks.add(month);

        RecyclerView rvMain = findViewById(R.id.rvMain);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        rvMain.setLayoutManager(lm);
        AdapterRecyclerMain adapter = new AdapterRecyclerMain(getApplicationContext(), blocks, new AdapterRecyclerMain.AdapterListener(){
            @Override
            public void btnOnClick(View v, int position) {
                Log.d(TAG, "btnOnClick: " + position);
            }
        });
        rvMain.setAdapter(adapter);

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
    public String getPrevMonthStartTimestamp(){ //getting the timestamp of the beginning of the month

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.AM_PM);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1);
        Long timestamp = cal.getTimeInMillis() / 1000;

        Log.d(TAG, "getPrevMonthStartTimestamp: " + String.valueOf(timestamp));
        return String.valueOf(timestamp);
    }

    public String getCurrentTimestamp(){
        Long timestamp = System.currentTimeMillis()/1000;
        return String.valueOf(timestamp);
    }

}
