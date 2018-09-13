package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
    }

    public class RequestToken{
        private int client_id;
        private String client_secret;
        private String code;
        private String access_token;
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
                Log.d(TAG, "internetCheck: internet OK");
            } else {
                Log.d(TAG, "internetCheck: NO internet");
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
}
