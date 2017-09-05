package com.min.imagewrapper.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.min.imagewrapper.R;
import com.min.imagewrapper.util.FilePathUtil;
import com.min.imagewrapper.util.ImageUtils;
import com.min.imagewrapper.util.L;
import com.min.imagewrapper.util.UIUtils;
import com.min.imagewrapper.view.CameraView;
import com.min.imagewrapper.view.FocusImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraViewActivity extends AppCompatActivity implements CameraView.TakePictureListener {

    private static String TAG=CameraViewActivity.class.getSimpleName();
    private static String DIR_CAMERA_IMAGES="my_camera";

    @Bind(R.id.cv_camera)
    CameraView mCamearCv;
    @Bind(R.id.fiv_focus)
    FocusImageView mFocusFIv;

    @Bind(R.id.iv_flash)
    ImageView mFlashIv;
    @Bind(R.id.iv_camera)
    ImageView mCamearIv;

    private boolean misTakingPic=false;  //当前如果正在拍照，禁用对焦和放大手势

    private final Camera.AutoFocusCallback mAutoFocusListener=new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {  //聚焦之后根据结果修改图片
                mFocusFIv.onFocusSuccess();
            }else {  //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                mFocusFIv.onFocusFailed();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        ButterKnife.bind(this);

        mCamearCv.setOnTouchListener(new TouchListener());
    }

    @OnClick(R.id.iv_camera)
    void clickBtnTakePicture(){
        mCamearCv.takePicture(this);
    }

    @OnClick(R.id.iv_flash)
    void clickFlash(){
        int nowFlashMode=mCamearCv.changeFlashMode();
        int imageResId=0;
        if(nowFlashMode==CameraView.FLASH_OFF){
            imageResId=R.drawable.camera_flash_off;
        }else if(nowFlashMode==CameraView.FLASH_ON){
            imageResId=R.drawable.camera_flash_on;
        }else if(nowFlashMode==CameraView.FLASH_AUTO){
            imageResId=R.drawable.camera_flash_auto;
        }
        if(imageResId>0){
            mFlashIv.setImageResource(imageResId);
        }
    }

    @OnClick(R.id.iv_change)
    void clickChangeCamera(){
        mCamearCv.changeCamera();
    }

    @Override
    public void onCameraStart(Camera camera) {
        mCamearIv.setClickable(false);
        misTakingPic=true;
    }

    @Override
    public void onCameraSuccess(byte[] data, Camera camera) {
        new SavePicTask(data).execute();
        mCamearCv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamearIv.setClickable(true);
                misTakingPic=false;

                mCamearCv.startPreview();
            }
        },100);
    }

    @Override
    public void onCameraError(Exception e) {
        L.e(TAG,e);
        mCamearCv.postDelayed(new Runnable() {
            @Override
            public void run() {
                UIUtils.toast(CameraViewActivity.this, "拍照失败！");

                mCamearIv.setClickable(true);
                misTakingPic=false;
            }
        }, 100);
    }

    private class SavePicTask extends AsyncTask<Void, Void, String> {

        private byte[] data;
        private File imageFile;

        SavePicTask(byte[] data) {
            this.data = data;

            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd_HHmmss_S");
            String fileName=sdf.format(new Date())+".jpeg";
            File dir=new File(FilePathUtil.getCacheRootDir(CameraViewActivity.this),DIR_CAMERA_IMAGES);
            if(!dir.exists()){
                dir.mkdirs();
            }
            imageFile = new File(dir,fileName);
        }

        protected void onPreExecute() {
            L.d(TAG, "照片开始处理");
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveToSDCard(data);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String saveToSDCard(byte[] data) throws IOException {
            String imageFilePath=null;
            try {
                Bitmap bp = BitmapFactory.decodeByteArray(data, 0, data.length);
                FileOutputStream fos=new FileOutputStream(imageFile);

                if(bp.compress(Bitmap.CompressFormat.JPEG, 60, fos)){
                    imageFilePath=imageFile.getAbsolutePath();
                }

                fos.flush();
                fos.close();
                if(bp!=null&&!bp.isRecycled()){
                    bp.recycle();
                }
                return imageFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageFilePath;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(TextUtils.isEmpty(result)){
                L.d(TAG, "照片保存失败 imagePath=%s",result);
            }else{
                L.d(TAG, "照片保存成功 imagePath=%s",result);
                try {
                    ImageUtils.notifyGallery(CameraViewActivity.this, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final class TouchListener implements View.OnTouchListener {

        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private static final int MODE_INIT = 0;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 1;
        private int mode = MODE_INIT;// 初始状态

        /** 用于记录拖拉图片移动的坐标位置 */
        private float startDis;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(misTakingPic){
                return false;
            }
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_INIT;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == MODE_ZOOM) {
                        //只有同时触屏两个点的时候才执行
                        if(event.getPointerCount()<2) return true;
                        float endDis = distance(event);// 结束距离
                        //每变化10f zoom变1
                        int scale=(int) ((endDis-startDis)/10f);
                        if(scale>=1||scale<=-1){
                            int zoom=mCamearCv.getZoom()+scale;
                            //zoom不能超出范围
                            if(zoom>mCamearCv.getMaxZoom()) zoom=mCamearCv.getMaxZoom();
                            if(zoom<0) zoom=0;
                            mCamearCv.setZoom(zoom);
                            //将最后一次的距离设为当前距离
                            startDis=endDis;
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    if(mode!=MODE_ZOOM){
                        //设置聚焦
                        Point point=new Point((int)event.getX(), (int)event.getY());
                        mCamearCv.onFocus(point,mAutoFocusListener);
                        mFocusFIv.startFocus(point);
                    }else {
                        //ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
                    }
                    break;
            }
            return true;
        }
        /** 计算两个手指间的距离 */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

    }

}
