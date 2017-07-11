package com.neilfvhv.www.deblur;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends BaseActivity {

    private ImageButton takeButton;
    private ImageButton chooseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takeButton = (ImageButton) findViewById(R.id.camera);
        chooseButton = (ImageButton) findViewById(R.id.picture);

        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // request permission
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA);
                while (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 222);
                    checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA);
                }

                // start intent
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(MainActivity.this,
                                "com.neilfvhv.www.deblur", new File(pictureRootPath + "/tempTake.png")));
                startActivityForResult(intent, TAKE_PICTURE);

                Toast.makeText(getApplicationContext(), "take picture", Toast.LENGTH_SHORT).show();
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // request permission
                int checkMediaPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                while (checkMediaPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 223);
                    checkMediaPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                // start intent
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, CHOOSE_PICTURE);

                Toast.makeText(getApplicationContext(), "choose picture", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra("flag", requestCode);
            if (requestCode == TAKE_PICTURE) {
                // store the path of temp picture
                intent.putExtra("filePath", pictureRootPath + "/tempTake.png");
            } else if (requestCode == CHOOSE_PICTURE) {
                // store the uri of chosen picture
                intent.setData(data.getData());
            }
            startActivity(intent);
        }
    }

}
