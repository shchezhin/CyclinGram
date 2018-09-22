package ru.cyclingram.cyclingram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PictureActivity extends Activity {

    private static final String TAG = "mytag";
    ImageView iv;
    Bitmap bmp;
    String txt;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        iv = findViewById(R.id.picView);
        saveBtn=findViewById(R.id.btnSave);

        // getting text
        Intent intent = getIntent();
        txt = intent.getStringExtra("text");

        // requesting to choose a photo
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(galleryIntent,"Select image"), 10);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // checking codes and getting bitmap from intent
        if (requestCode == 10)
        {
            if (resultCode == Activity.RESULT_OK && data != null)
            {
                try {
                    bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                }
                catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something's wrong", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        // creating canvas from bitmap and drawing text on it
        Bitmap.Config config = bmp.getConfig();
        if(config==null) {
            config=Bitmap.Config.ARGB_8888;
        }
        final Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), config);
        Canvas canvas = new Canvas(newBmp);
        canvas.drawBitmap(bmp,0,0,null);
        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(300);
        paintText.setStyle(Paint.Style.FILL_AND_STROKE);
        Rect textRect = new Rect();
        paintText.getTextBounds(txt, 0,txt.length(),textRect);
        float shadow = 10;
        while (textRect.width() > canvas.getWidth()-(paintText.getTextSize())) {
            paintText.setTextSize(paintText.getTextSize()-2);
            paintText.getTextBounds(txt, 0, txt.length(), textRect);
            shadow = paintText.getTextSize()/10;
        }
        paintText.setShadowLayer(shadow,shadow,shadow, Color.BLACK);
        canvas.drawText(txt, (bmp.getWidth()/2)-(textRect.width()/2),bmp.getHeight()/2, paintText);

        //showing created image to user
        iv.setImageBitmap(newBmp);
        iv.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBtn.setEnabled(false);
                iv.setVisibility(View.INVISIBLE);
                saveBtn.setVisibility(View.INVISIBLE);

                AsyncPicSaver saver = new AsyncPicSaver(getApplicationContext(),newBmp);
                saver.execute();
                Toast.makeText(getApplicationContext(), "Saving picture...", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
