package ru.cyclingram.cyclingram;

import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AsyncRetroStats extends AsyncTask <String, Void, ArrayList<ActivityAPI>> {

    private static final String TAG = "mytag";

    @Override
    protected ArrayList<ActivityAPI> doInBackground(String... strings) {

        String after = strings[0];
        String before = strings[1];
        String token = strings[2];
        ArrayList<ActivityAPI> result = new ArrayList<>();

        Retrofit statsRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.strava.com/api/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StravaApi activitiesStravaApi = statsRetrofit.create(StravaApi.class);
        Call<ArrayList<ActivityAPI>> requestingActivities = activitiesStravaApi.requestActivities(
                before, after, "1","90", token);
        try {
            result = requestingActivities.execute().body();
        }catch(Exception e){
            Log.d(TAG, "AsyncRetroStats doInBackground Exception: " + e.getMessage());
        }

        return result;
    }

    public interface StravaApi {

        @GET("athlete/activities")
        Call<ArrayList<ActivityAPI>> requestActivities(@Query("before") String before,
                                               @Query("after") String after,
                                               @Query("page") String page,
                                               @Query("per_page") String per_page,
                                               @Query("access_token") String access_token);
    }


}
