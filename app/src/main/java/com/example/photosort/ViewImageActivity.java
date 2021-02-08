package com.example.photosort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity {
    // private ImageView imageView;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    private static int RESULT_DELETE_IMAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Intent intent = getIntent();
        String picturePath = intent.getStringExtra(MainActivity.EXTRA_PICTURE_PATH);
        Uri pictureUri = intent.getData();

        ImageView imageView = (ImageView) findViewById(R.id.ivPreview);
        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        // Delete Button
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver contentResolver = getContentResolver();
                Intent deleteIntent = new Intent(Intent.ACTION_DELETE, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                deleteIntent.setData(pictureUri);
                deleteIntent.putExtra(MainActivity.EXTRA_PICTURE_PATH, picturePath);
                System.out.println("PICTURE PATH " + picturePath);

                deleteIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                deleteIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                File fileToDelete = new File(picturePath);
                System.out.println("Deleting: " + fileToDelete.delete());

                List<ResolveInfo> resInfoList = view.getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    System.out.println("PACKAGE NAME: " + packageName);
                    view.getContext().grantUriPermission("com.google.android.apps.photos.contentprovider", pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    view.getContext().grantUriPermission("com.google.android.apps.photos.contentprovider.impl.MediaContentProvider", pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                  //  view.getContext().grantUriPermission("com.example.photosort", pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                System.out.println("Has storage permissoin? " + hasStoragePermission());
                System.out.println("Hardcoded delete: " + deleteFile("/storage/emulated/0/DCIM/Camera/PXL_20210205_125540225.jpg"));
                DeleteAndScanFile(view.getContext(), picturePath, fileToDelete);
                refreshGallery(view.getContext(), picturePath);
                contentResolver.delete (pictureUri,null ,null);
                // Intent chooser = Intent.(deleteIntent, "Chooser Title");
//                chooser.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//                chooser.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                view.getContext().startActivity(chooser);

//                deleteIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                deleteIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                 view.getContext().startActivity(deleteIntent);
                startActivityForResult(deleteIntent, RESULT_DELETE_IMAGE);

            }

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Request code: " + requestCode);
        System.out.println("Response code: " + resultCode);
        System.out.println("data: " + data);

        if (requestCode == RESULT_DELETE_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image
            Intent intent = new Intent(this, DeleteImageActivity.class);
            intent.putExtra(MainActivity.EXTRA_PICTURE_PATH, picturePath);
            intent.setData(selectedImage);
            startActivity(intent);
        }
    }

    private void DeleteAndScanFile(final Context context, String path,
                                   final File fi) {
        String fpath = path.substring(path.lastIndexOf("/") + 1);
        Log.i("fpath", fpath);
        System.out.println("fpath: " + fpath);
        try {
            MediaScannerConnection.scanFile(
                    context,
                    new String[] { Environment.getExternalStorageDirectory().toString() + "/images/" + fpath.toString()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            System.out.println("URI : " + uri);
                            if (uri != null) {
                                context.getContentResolver().delete(uri, null,
                                        null);
                            }
                            fi.delete();
                            System.out.println("file Deleted :" + fi.getPath());
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
            e.printStackTrace();
        }
    }

    public static void refreshGallery(Context context, String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            System.out.println("IN EXISTING");
            System.out.println("DELETING: " + file.delete());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            System.out.println("SPECIAL CASE");
            Intent intent= new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            context.sendBroadcast(intent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(file.getAbsolutePath())));
        }
    }

    public  boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission is granted");
                return true;
            } else {

                System.out.println("Permission error");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            System.out.println("Permission is granted");
            return true;
        }
    }
}