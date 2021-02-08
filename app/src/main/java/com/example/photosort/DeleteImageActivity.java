package com.example.photosort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DeleteImageActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SECURE_SETTINGS,
            Manifest.permission.DELETE_PACKAGES,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.MEDIA_CONTENT_CONTROL
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_image);
        verifyStoragePermissions(this);

        Intent intent = getIntent();
        String picturePath = intent.getStringExtra(MainActivity.EXTRA_PICTURE_PATH);
        Uri pictureUri = intent.getData();

        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            System.out.println("PACKAGE NAME: " + packageName);
            this.grantUriPermission("com.google.android.apps.photos.contentprovider", pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        grantUriPermission("com.example.photosort", pictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //grantUriPermission("com.example.photosort", pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        ContentResolver contentResolver = getContentResolver();
        System.out.println("PICTURE PATH: " + picturePath);
        System.out.println("PICTURE URI: " + pictureUri);
        File fileToDelete = new File(picturePath);
        File fileTwo = new File(pictureUri.getPath());
        if (fileToDelete.exists()) {
            try {
                System.out.println("DELETING 1: " + fileToDelete.getCanonicalFile().delete());
                System.out.println("DELETING 2: " + fileTwo.delete());
                System.out.println("DELETING 4: " + deleteFile("PXL_20210205_125540225.jpg"));
            } catch (IOException e) {
                System.out.println("Catching Error: " + e);
                e.printStackTrace();
            }
            System.out.println("DELETING URI");
            contentResolver.delete (pictureUri,null ,null);
            if (fileToDelete.delete()) {
                System.out.println("file Deleted :" + picturePath);
            } else {
                System.out.println("file not Deleted :" + picturePath);
            }
        }
    }

    // Checks if the app has permission to write to device storage
    // If the app does not has permission then the user will be prompted to grant permissions
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}