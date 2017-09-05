package com.min.album.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by minyangcheng on 2016/11/24.
 */

public class CameraHelper {

    private static final int REQ_PICK_PICTURE_FROM_CAMERA = 7459;

    private AmountLimitCache mCache;

    private int MAX_IAMGE_FILE_COUNT=20;  //保留的最大图片个数
    private String DIR_IMAGE="albumCameraPhoto";

    private static String mSuffix=".jpeg";
    private String mCameraImageUriStr;

    public interface Callbacks {
        void onImagePickerError(Exception e);

        void onImagePicked(File imageFile);
    }

    public CameraHelper(Context context){
        File cacheDir=new File(getCacheRootDir(context),DIR_IMAGE);
        mCache=new AmountLimitCache(cacheDir.getAbsolutePath());
        mCache.setCacheSize(MAX_IAMGE_FILE_COUNT);
    }

    public void openCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File image = getImageFile();
        Uri capturedImageUri = Uri.fromFile(image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        mCameraImageUriStr = capturedImageUri.toString();
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_CAMERA);
    }

    private File pickedCameraPicture() throws IOException, URISyntaxException {
        mCache.deleteFilesWhenArriveMaxCount();
        URI imageUri = new URI(mCameraImageUriStr);
        return new File(imageUri);
    }

    public File getImageFile(){
        File imageFile = mCache.getFile(getImageFileNameByTime());
        return imageFile;
    }

    private String getImageFileNameByTime(){
        return System.currentTimeMillis()+mSuffix;
    }

    public static File getCacheRootDir(Context context){
        File dir=context.getExternalCacheDir();
        if(dir==null){
            dir=context.getCacheDir();
        }
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    public void handleActivityResult(int requestCode, int resultCode,final Callbacks callbacks) {
        if(resultCode != AppCompatActivity.RESULT_OK){
            return;
        }
        if(requestCode == REQ_PICK_PICTURE_FROM_CAMERA) {
            try {
                File photoFile = pickedCameraPicture();
                callbacks.onImagePicked(photoFile);
            } catch (Exception e) {
                callbacks.onImagePickerError(e);
            }
        }
    }

}
