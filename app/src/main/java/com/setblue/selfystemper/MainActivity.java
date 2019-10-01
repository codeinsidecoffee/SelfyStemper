package com.setblue.selfystemper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TAG";
    Context context;
    ImageView finalImage;
    Button btn_capture;
    private int INTENT_CAMERA_CODE = 100;
    private Bitmap imageBitmap;
    private Uri fileUri;
    public static String filePath = null;
    public static final String IMAGE_NAME = "profileWithWaterMark";
    private Bitmap finalBitmap;
    private Bitmap logoBitmap,tempBitMap;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;

        checkMyPermission();

        btn_capture=findViewById(R.id.btn_capture);
        finalImage=findViewById(R.id.finalImage);
        btn_capture.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_capture){
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File

                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, INTENT_CAMERA_CODE);
                }
            }


        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkMyPermission() {
        String[] permissionArrays = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, 11111);
        } else {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean openActivityOnce = true;
        boolean openDialogOnce = true;
        if (requestCode == 11111) {
            boolean isPermitted=false;
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                isPermitted = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        //execute when 'never Ask Again' tick and permission dialog not show
                    } else {
                        if (openDialogOnce) {
                            alertView();
                        }
                    }
                }
            }

        }
    }
    private void alertView() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle("Permission Denied")
                .setInverseBackgroundForced(true)
                //.setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Without those permission the app is unable to save your profile. App needs to save profile image in your external storage and also need to get profile image from camera or external storage.Are you sure you want to deny this permission?")

                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                    }
                })
                .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        checkMyPermission();

                    }
                }).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INTENT_CAMERA_CODE) {

            if (resultCode == RESULT_OK) {

                 logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.acd);

                try {

                    Log.e(TAG, "onActivityResult: "+currentPhotoPath );

                     imageBitmap=null;
                    File f= new File(currentPhotoPath);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);



                } catch (IOException e) {
                    e.printStackTrace();
                }





                finalBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(finalBitmap);

                logoBitmap = scaleDown(logoBitmap,440, 440);

                logoBitmap.setHasAlpha(true);

                canvas.drawBitmap(imageBitmap, 0, 0, null);
                canvas.drawBitmap(logoBitmap, 100, 100, null);
                finalImage.setImageBitmap(finalBitmap);

                btn_capture.setVisibility(View.GONE);
            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_cancelled_image_capture), Toast.LENGTH_SHORT).show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sorry_failed_to_capture_image), Toast.LENGTH_SHORT).show();
            }
        }
    }





    public static Bitmap scaleDown(Bitmap realImage, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, false);
        return newBitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "temp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );


        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


}
