package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class MainActivity extends Activity {

    private static final String TAG = "mytag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking the stored Strava API token. if no token, requesting it
        if(getStoredToken().length()!=40){authStrava();}

        requestRides(getWeekStartTimestamp(), getCurrentTimestamp());
    }

    public void authStrava(){

        //checking for internet, if success - starting Strava Login Activity to authenticate
        if (internetCheck()){
            Intent intent = new Intent(this, StravaLoginActivity.class);
            startActivityForResult(intent, 777);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No internet connection",  Toast.LENGTH_LONG);
            toast.show(); //will handle logic here later
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //getting the received code and requesting a token via Retrofit
        if (data == null) {return;}
        String code = data.getStringExtra("code");

        Retrofit stravaRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.strava.com/oauth/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StravaApi tokenStravaApi = stravaRetrofit.create(StravaApi.class);

        Call<RequestToken> requestingToken = tokenStravaApi.requestToken(25912,"fb921f750be783b4b8f4f44eaa80b7020c7aa657", code);
        requestingToken.enqueue(new Callback<RequestToken>() {
            @Override
            public void onResponse(Call<RequestToken> call, Response<RequestToken> response) {
                if (response.isSuccessful()) {
                    if(response.body().access_token != null){
                        saveToken(response.body().access_token);
                    }
                }
            }
            @Override
            public void onFailure(Call<RequestToken> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public interface StravaApi {

        @POST("token")
        @FormUrlEncoded
        Call<RequestToken> requestToken(@Field("client_id") int client_id,
                                    @Field("client_secret") String client_secret,
                                    @Field("code") String code);

        @GET("athlete/activities")
        Call<List<Activity>> requestActivities(@Query("before") String before,
                                               @Query("after") String after,
                                               @Query("page") String page,
                                               @Query("per_page") String per_page,
                                               @Query("access_token") String access_token);
    }

    public class RequestToken {
        private int client_id;
        private String client_secret;
        private String code;
        private String access_token;
    }

    public class Activity {
        private long id;
        private String name;
        private float distance;
        private float moving_time;
        private String type;
        private String workout_type;
        private boolean commute;
    }

    public void requestRides(int after, int before){
        Retrofit statsRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.strava.com/api/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StravaApi activitiesStravaApi = statsRetrofit.create(StravaApi.class);
        Call<List<Activity>> requestingActivities = activitiesStravaApi.requestActivities(
                String.valueOf(before),String.valueOf(after), "1","90", getStoredToken());
        Log.d(TAG, "is internet: " + internetCheck());
        requestingActivities.enqueue(new Callback<List<Activity>>() {
            @Override
            public void onResponse(Call<List<Activity>> call, Response<List<Activity>> response) {
                if (response.isSuccessful()) {
                    doSmth(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Activity>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void doSmth (List<Activity> activities){
        float commutes = 0, nonCommutes = 0;
        for(int i=0; i<activities.size(); i++){
            //Log.d(TAG, "Name: " + activities.get(i).name + " kms: " + activities.get(i).distance/1000 + " commute: " + activities.get(i).commute);
            if(activities.get(i).commute) {commutes = commutes + activities.get(i).distance/1000;}
            if(!activities.get(i).commute) {nonCommutes = nonCommutes + activities.get(i).distance/1000;}
        }
        Log.d(TAG, "commuted: " + commutes);
        Log.d(TAG, "rides: " + nonCommutes);
        Log.d(TAG, "total kms: " + (commutes+nonCommutes) );
        TextView tvKms = findViewById(R.id.tv_kms);
        float kms = commutes+nonCommutes;
        String formattedKms = new DecimalFormat("#0.00").format(kms);
        tvKms.setText(String.valueOf(formattedKms));
    }

    public boolean internetCheck(){
        InternetCheck check = new InternetCheck();
        boolean inet = false;
        try{
            inet = check.execute().get();
        }catch(Exception e){
            Log.d(TAG, "authStrava: " + e.getMessage());
        }
        finally {
            if (inet){
                //Log.d(TAG, "internetCheck: internet OK");
            } else {
                //Log.d(TAG, "internetCheck: NO internet");
            }
            return inet;
        }
    }

    public void saveToken(String token){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString("StravaToken", token);
        prefsEditor.commit();
    }

    public String getStoredToken(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        String token = appSharedPrefs.getString("StravaToken","");
        return token;
    }

    public int getWeekStartTimestamp(){ //getting the timestamp of the beginning of the week

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.AM_PM);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Long timestamp = cal.getTimeInMillis() / 1000;
        int inted = timestamp.intValue();

        return inted;
    }

    public int getMonthStartTimestamp(){ //getting the timestamp of the beginning of the month

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.AM_PM);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Long timestamp = cal.getTimeInMillis() / 1000;
        int inted = timestamp.intValue();

        return inted;
    }

    public int getCurrentTimestamp(){
        Long timestamp = System.currentTimeMillis()/1000;
        int inted = timestamp.intValue();
        return inted;
    }

}
