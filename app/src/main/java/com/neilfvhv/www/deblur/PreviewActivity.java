package com.neilfvhv.www.deblur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PreviewActivity extends BaseActivity {

    private CustomProgressDialog dialog;
    private ImageButton runButton;
    private ImageView previewImage;
    private Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        dialog = new CustomProgressDialog(PreviewActivity.this, R.drawable.anim);
        runButton = (ImageButton) findViewById(R.id.run);
        previewImage = (ImageView) findViewById(R.id.preview);
        preview = new Preview();

        Intent intent = getIntent();
        if (intent != null) {
            // get bitmap and set preview image
            if (intent.getIntExtra("flag", CHOOSE_PICTURE) == TAKE_PICTURE) {
                // transform file path to bitmap
                preview.setBitmap(BitmapFactory.decodeFile(intent.getStringExtra("filePath")));
                previewImage.setImageBitmap(preview.getBitmap());
                // delete temp take file
                File tempTake = new File(intent.getStringExtra("filePath"));
                if (tempTake.exists()) {
                    if (!tempTake.delete()) {
                        Toast.makeText(this, "delete temp file error", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (intent.getIntExtra("flag", CHOOSE_PICTURE) == CHOOSE_PICTURE) {
                // transform uri to bitmap
                try {
                    preview.setBitmap(BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(intent.getData())
                    ));
                    previewImage.setImageBitmap(preview.getBitmap());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            preview.setBitmap(compressImage(preview.getBitmap()));
            // generate temp send file
            File tempSend = new File(pictureRootPath + "/tempSend.png");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tempSend);
                if (preview.getBitmap() != null) {
                    preview.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                preview.setTempSend(tempSend);
            }
        }

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show dialog
                //dialog.show();
                // network request
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        byte resultBytes[] = null;
                        Socket socket = null;
                        DataOutputStream dos = null;
                        FileInputStream fis = null;
                        DataInputStream dis = null;

                        try {

                            socket = new Socket();
                            // time out after 20 seconds
                            socket.setSoTimeout(20 * 1000);
                            // connect
                            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

                            dos = new DataOutputStream(socket.getOutputStream());

                            fis = new FileInputStream(preview.getTempSend());

                            // send length of file
                            dos.writeLong(preview.getTempSend().length());

                            byte bufferedBytes[] = new byte[1024];
                            int length;
                            int totalLength = 0;

                            // send bytes
                            while ((length = fis.read(bufferedBytes, 0, bufferedBytes.length)) > 0) {
                                dos.write(bufferedBytes, 0, length);
                                dos.flush();
                            }

                            dis = new DataInputStream(socket.getInputStream());

                            // get length of file
                            resultBytes = new byte[dis.readInt()];

                            // get bytes
                            while ((length = dis.read(bufferedBytes, 0, bufferedBytes.length)) > 0) {
                                System.arraycopy(bufferedBytes, 0, resultBytes, totalLength, length);
                                totalLength += length;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (socket != null){
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (dos != null){
                                try {
                                    dos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fis != null){
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(dis!=null){
                                try {
                                    dis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            // delete temp send file
                            if (preview.getTempSend().exists()) {
                                if (!preview.getTempSend().delete()) {
                                    Toast.makeText(PreviewActivity.this, "delete temp file error", Toast.LENGTH_SHORT).show();
                                }
                            }
                            // cancel dialog
                            //dialog.cancel();
                            // from PreviewActivity to ResultActivity
                            Intent intent = new Intent(PreviewActivity.this, ResultActivity.class);
                            intent.putExtra("resultBytes", resultBytes);
                            startActivity(intent);
                        }
                    }
                }).start();

            }
        });

    }


    private class Preview {

        private Bitmap bitmap;
        private File tempSend;

        Bitmap getBitmap() {
            return bitmap;
        }

        void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        File getTempSend() {
            return tempSend;
        }

        void setTempSend(File tempSend) {
            this.tempSend = tempSend;
        }
    }
}
