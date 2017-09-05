package com.min.imagewrapper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.min.album.Album;
import com.min.imagewrapper.R;
import com.min.imagewrapper.util.GsonUtil;
import com.min.imagewrapper.util.L;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomImageSelectorActivity extends AppCompatActivity {

    private static final String TAG="CustomImageSelectorActivity_TEST";

    @Bind(R.id.tv_result)
    TextView mResultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_image_selector);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_single)
    void clickBtnForSingle(){
        Album.startAlbumForSingleChoice(this);
    }

    @OnClick(R.id.btn_mutil_four)
    void clickBtnForMutilFour(){
        Album.startAlbumForMultiChoice(this,4,null);
    }

    @OnClick(R.id.btn_mutil_pre)
    void clickBtnForMutilPre(){
        ArrayList<String> pathList=new ArrayList<>();
        pathList.add("/storage/emulated/0/DCIM/Camera/plateID_20161126_170827.jpg");
        pathList.add("/storage/emulated/0/DCIM/Camera/plateID_20161110_163801.jpg");
        Album.startAlbumForMultiChoice(this,4,pathList);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Album.handleActivityResult(requestCode, resultCode, data, new Album.OnResultListener() {
            @Override
            public void onResult(ArrayList<String> resultList) {
                mResultTv.setText(GsonUtil.toPrettyJson(resultList));
                L.d(TAG, GsonUtil.toPrettyJson(resultList));
            }
        });
    }

}
