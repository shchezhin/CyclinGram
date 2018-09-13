package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import okhttp3.HttpUrl;

public class StravaLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_login);

        //going to Strava auth URL and catching the response code

        HttpUrl authorizeUrl = HttpUrl.parse("https://www.strava.com/oauth/authorize") //
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
                        Intent intent = new Intent();
                        intent.putExtra("code", code);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        //later add some logic for if the code is null
                    }
                    return true;
                }
                else {return false;}
            }
        });

        loginWebView.loadUrl(String.valueOf(authorizeUrl));
    }
}
