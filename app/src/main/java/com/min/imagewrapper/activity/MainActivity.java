package com.min.imagewrapper.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.min.imagewrapper.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    &&ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                    1000);
        }
    }

    @OnClick(R.id.btn_system_camear_gallery)
    void clickBtnSystemGalleryAndCamera() {
        //调用系统相机或图库选择图片
        go(EasyImageSelectorActivity.class);
    }

    @OnClick(R.id.btn_custom_album)
    void clickBtnCustomAlbum() {
        //调用自定义图片选择器
        go(CustomImageSelectorActivity.class);
    }

    @OnClick(R.id.btn_custom_camear)
    void clickBtnCustomCamera() {
        //调用自定义相机
        go(CameraViewActivity.class);
    }

    private void go(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

}
