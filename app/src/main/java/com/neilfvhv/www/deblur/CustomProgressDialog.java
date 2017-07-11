package com.neilfvhv.www.deblur;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;


class CustomProgressDialog extends ProgressDialog {

    private AnimationDrawable animationDrawable;
    private ImageView imageView;
    private int resourceId;

    CustomProgressDialog(Context context, int resourceId) {
        super(context);
        this.resourceId = resourceId;
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.progress_dialog);
        imageView = (ImageView) findViewById(R.id.loadingIv);
    }

    private void initData() {
        imageView.setBackgroundResource(resourceId);
        animationDrawable = (AnimationDrawable) imageView.getBackground();
        imageView.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });
    }

}