package com.min.imagewrapper.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.min.imagewrapper.R;
import com.min.imagewrapper.http.HttpClient;
import com.min.imagewrapper.util.EasyImageSelector;
import com.min.imagewrapper.util.ExifInterfaceUtil;
import com.min.imagewrapper.util.ImageLoaderWrap;
import com.min.imagewrapper.util.ImageUtils;
import com.min.imagewrapper.util.L;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EasyImageSelectorActivity extends AppCompatActivity {

    private static final String TAG="EasyImageSelectorActivity_TEST";

    //请指定图片上传地址
    private static final String UPLOAD_IMAGE_URL="http://file.cheguo.com/upload/uploadfileforaliyun.do?filesource=10f4fe1edeae11e5b7be086266812821&extname=.jpg";

    @Bind(R.id.iv_image)
    ImageView mImageIv;

    private File mOriginalImageFile;
    private File mCompressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_select_image)
    void clickSelectImageIv(){
        String[] items={"拍照","图库"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("选择照片方式")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            EasyImageSelector.getInstance(getActivity()).openCamera(getActivity());
                        }else{
                            EasyImageSelector.getInstance(getActivity()).openGalleryPicker(getActivity());
                        }
                    }
                });
        builder.show();
    }

    private AppCompatActivity getActivity(){
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImageSelector.getInstance(getActivity()).handleActivityResult(requestCode, resultCode, data, this, new EasyImageSelector.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImageSelector.ImageSource source) {
                L.d(TAG, "onImagePickerError exception=%s", e);
            }

            @Override
            public void onImagePicked(final File imageFile, EasyImageSelector.ImageSource source) {
                L.d(TAG, "onImagePicked file=%s", imageFile.getAbsolutePath());
                final File outFile=EasyImageSelector.getInstance(getActivity()).getImageFile();
                if(ImageUtils.compressImageToFile(imageFile, outFile, 500, 500)){
                    ImageLoaderWrap.displayFileImage(imageFile, mImageIv);
                    new Thread(){
                        @Override
                        public void run() {
                            mOriginalImageFile=imageFile;
                            mCompressedImageFile=outFile;
                            setExifInfoInImageFile(mCompressedImageFile);

                            uploadImageFile(outFile);
                        }
                    }.start();
                }else{
                    onImagePickerError(new Exception("图片压缩失败"),source);
                }
            }
        });
    }

    private void uploadImageFile(File imageFile){
        L.d(TAG, "uploadImageFile outFile=%s", imageFile.getAbsolutePath());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""),
                        RequestBody.create(null, "123456789"))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""),
                        RequestBody.create(null, "123456789"))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"image\" ;filename=\"\""),
                        RequestBody.create(HttpClient.IMAGE, imageFile))
                .build();
        Request request = new Request.Builder()
                .url(UPLOAD_IMAGE_URL)
                .post(requestBody)
                .build();
        Call call = HttpClient.okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.d(TAG, "onFailure=%s", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.d(TAG, "onResponse=%s", response.body().string());
                getExifInfoInImageFile(mCompressedImageFile);
            }
        });
    }

    private void getExifInfoInImageFile(File imageFile){
        try {
            ExifInterface exifInterface=new ExifInterface(imageFile.getAbsolutePath());
            float[] floatArr=ExifInterfaceUtil.getLatitudeAndLongtitude(exifInterface);
            String latitudeStr=exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitudeStr=exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String dateTimeStr=exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            L.d(TAG,"TAG_GPS_LATITUDE=%s , TAG_GPS_LONGITUDE=%s" +
                            ", floatArr=%s , TAG_DATETIME=%s"
                    , latitudeStr,longitudeStr,floatArr[0]+"_"+floatArr[1]
                    , dateTimeStr);
        } catch (IOException e) {
            L.e(TAG,e);
        }
    }

    private void setExifInfoInImageFile(File imageFile){
        try {
            ExifInterface exifInterface=new ExifInterface(imageFile.getAbsolutePath());
            ExifInterfaceUtil.setLatitudeAndLongtitude(exifInterface,21.45,49.536);
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME,"2017:11:20 19:06:21");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            L.e(TAG,e);
        }
    }

}
