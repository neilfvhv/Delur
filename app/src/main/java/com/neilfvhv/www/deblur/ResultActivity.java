package com.neilfvhv.www.deblur;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResultActivity extends BaseActivity {

    private ImageView resultImage;
    private ImageButton saveButton;
    private Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImage = (ImageView) findViewById(R.id.result);
        saveButton = (ImageButton) findViewById(R.id.save);
        result = new Result();

        result.setSavePath(pictureRootPath + "/deblur" + java.util.UUID.randomUUID().toString() + ".png");

        Intent intent = getIntent();
        if (intent != null) {
            // get result bytes
            result.setResultBytes(intent.getByteArrayExtra("resultBytes"));
            // set result image
            if (result.getResultBytes().length != 0) {
                resultImage.setImageBitmap(BitmapFactory.decodeByteArray(
                        result.getResultBytes(), 0, result.getResultBytes().length
                ));
            }
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(result.getSavePath());
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos);
                    bos.write(result.getResultBytes());
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Toast.makeText(ResultActivity.this, "image saved", Toast.LENGTH_SHORT).show();

                // from ResultActivity to MainActivity
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

    }

    private class Result {

        private byte resultBytes[];
        private String savePath;

        byte[] getResultBytes() {
            return resultBytes;
        }

        void setResultBytes(byte[] resultBytes) {
            this.resultBytes = resultBytes;
        }

        String getSavePath() {
            return savePath;
        }

        void setSavePath(String savePath) {
            this.savePath = savePath;
        }
    }
}
