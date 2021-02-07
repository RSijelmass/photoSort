package com.example.photosort;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class ViewImageActivity extends AppCompatActivity {
    // private ImageView imageView;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Intent intent = getIntent();
        String picturePath = intent.getStringExtra(MainActivity.EXTRA_PICTURE);

        ImageView imageView = (ImageView) findViewById(R.id.ivPreview);
        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        System.out.println("DATA IN VIEWIMAGE: " + picturePath);
    }
}