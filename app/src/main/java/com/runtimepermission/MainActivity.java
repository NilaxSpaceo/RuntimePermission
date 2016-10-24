package com.runtimepermission;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int APP_PERMISSION = 10;
    private final int APP_DETAIL_SETTINGS = 11;
    private final int APP_IMG_RC = 12;
    private ImageView ivPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();
    }

    private void initControls() {
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
    }

    public void onPickImageClicked(View view) {
        if (isPermissionRequired())
            return;

        pickImageFromGallery();
    }


    public boolean isPermissionRequired() {

        List<String> mListPermission = new ArrayList<>();


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            mListPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }


        if (mListPermission.size() == 0) {
            return false;
        } else {
            String[] arrayPermission = new String[mListPermission.size()];
            ActivityCompat.requestPermissions(this,
                    mListPermission.toArray(arrayPermission),
                    APP_PERMISSION);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case APP_PERMISSION: {

                String msg = "";
                // If request is cancelled, the result arrays are empty.
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
                            msg = getString(R.string.msg_dialog_no_storage_permission);
                            break;
                        }
                    }
                }
                if (!msg.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setMessage(msg)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Intent to change permission of application
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, APP_DETAIL_SETTINGS);
                                }
                            });
                    // Create the AlertDialog object and return it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED))
                    pickImageFromGallery();

            }
        }

    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, APP_IMG_RC);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == APP_DETAIL_SETTINGS) {
            isPermissionRequired();
        } else if (requestCode == APP_IMG_RC && resultCode == RESULT_OK
                && null != data) {

            try {
                Uri URI = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};


                Cursor cursor = getContentResolver().query(URI,
                        FILE, null, null, null);

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(FILE[0]);
                String mImageDecode = cursor.getString(columnIndex);
                cursor.close();

                ivPhoto.setImageBitmap(BitmapFactory
                        .decodeFile(mImageDecode));

            } catch (Exception e) {
                Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                        .show();
            }


        }
    }
}
