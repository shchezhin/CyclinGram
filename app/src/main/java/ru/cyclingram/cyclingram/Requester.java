package ru.cyclingram.cyclingram;

import android.util.Log;

import java.util.ArrayList;

public class Requester {

    private static final String TAG = "mytag";

    public float request (String after, String before, String token){


        AsyncRetroStats requester = new AsyncRetroStats();
        requester.execute(after,before,token); //Strings: after, before, token
        ArrayList<ActivityAPI> thisWeek = new ArrayList<>();
        try {
            thisWeek = requester.get();
        }catch(Exception e){
            Log.d(TAG, "Async get exception: " + e.getMessage());
        }

        float thisKms=0, thisTime=0;
        for (int i=0;i<thisWeek.size();i++){
            thisKms = thisKms + thisWeek.get(i).distance;
            thisTime = thisTime + thisWeek.get(i).moving_time;
        }

        thisKms=thisKms/1000;
        thisTime=thisTime/3600;

        float output=0;
        if(thisKms != 0){
        output = Float.valueOf( String.format("%.1f", thisKms/thisTime) );
        }

        return output;
    }
}
