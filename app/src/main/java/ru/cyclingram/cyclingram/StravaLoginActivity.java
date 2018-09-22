package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class StravaLoginActivity extends Activity {

    private static final String TAG = "mytag";
    ImageButton stravaBtn;
    TextView tv_btn;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_login);

        tv_btn = findViewById(R.id.tv_btn);
        pb = findViewById(R.id.progressBar);
        stravaBtn = findViewById(R.id.stravaBtn);

        stravaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuth();
            }
        });
    }

    private void startAuth() {
        pb.setVisibility(View.VISIBLE);
        stravaBtn.setVisibility(View.INVISIBLE);
        tv_btn.setVisibility(View.INVISIBLE);

        //going to Strava auth URL and catching the response code
        final HttpUrl authorizeUrl = HttpUrl.parse("https://www.strava.com/oauth/authorize") //
                .newBuilder() //
                .addQueryParameter("client_id", "25912")
                .addQueryParameter("scope", "public")
                .addQueryParameter("redirect_uri", "http://cyclingram.ru/auth")
                .addQueryParameter("approval_prompt", "auto")
                .addQueryParameter("response_type", "code")
                .build();

        final WebView loginWebView = findViewById(R.id.loginWebview);
        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wView, String url) {

                if (url.contains("http://cyclingram.ru/auth")) {
                    Uri myUri = Uri.parse(url);
                    if(myUri.getQueryParameter("code")!=null) {
                        String code = myUri.getQueryParameter("code");
                        auth(code);
                    } else {
                        //later add some logic for if the code is null
                    }
                    return true;
                }
                else {return false;}
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pb.setVisibility(View.INVISIBLE);
            }
        });

        loginWebView.loadUrl(String.valueOf(authorizeUrl));
    }

    private void auth(String code) {

        //getting the received code and requesting a token via Retrofit
        Retrofit stravaRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.strava.com/oauth/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StravaApi tokenStravaApi = stravaRetrofit.create(StravaApi.class);

        Call<RequestToken> requestingToken = tokenStravaApi.requestToken(25912, "fb921f750be783b4b8f4f44eaa80b7020c7aa657", code);
        requestingToken.enqueue(new Callback<RequestToken>() {
            @Override
            public void onResponse(Call<RequestToken> call, Response<RequestToken> response) {
                if (response.isSuccessful()) {
                    if (response.body().access_token != null) {
                        saveToken(response.body().access_token);
                        finish();
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

    public void saveToken(String token) {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString("StravaToken", token);
        prefsEditor.apply();
    }
}
