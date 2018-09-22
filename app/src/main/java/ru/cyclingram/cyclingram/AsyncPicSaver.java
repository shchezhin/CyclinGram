package ru.cyclingram.cyclingram;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AsyncPicSaver extends AsyncTask<Bitmap,Void,Void> {
    private Context mContext;
    private Bitmap mBitmap;

    public AsyncPicSaver (Context context, Bitmap bitmap){
        mContext = context;
        mBitmap = bitmap;
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        OutputStream myOutputStream = null;
        File myFile = new File(Environment.getExternalStorageDirectory() + File.separator + "cyclinpic.jpg");
        try{
            myOutputStream = new FileOutputStream(myFile);
        } catch (FileNotFoundException e){}
        mBitmap.compress(Bitmap.CompressFormat.JPEG,100, myOutputStream);
        try {
            myOutputStream.flush();
        } catch (IOException | NullPointerException e) {}
        try {
            myOutputStream.close();
        } catch (IOException | NullPointerException e) {}
        try{
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(), myFile.getAbsolutePath(), myFile.getName(), myFile.getName());
        } catch (FileNotFoundException e) {}

        return null;
    }
}
