package com.min.imagewrapper.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by minyangcheng on 2016/4/26.
 */
public class ImageUtils {

    public static boolean compressImageToFile(InputStream is,File outFile,int reqWidth,int reqHeight) {
        boolean flag=false;
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize=calculateInSampleSize(options.outWidth,options.outHeight,reqWidth,reqHeight);
            options.inJustDecodeBounds=false;
            options.inPreferredConfig= Bitmap.Config.RGB_565;
            Bitmap bitmap=BitmapFactory.decodeStream(is, null, options);

            FileOutputStream fos=new FileOutputStream(outFile);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos)){
                flag=true;
            }
            fos.flush();
            fos.close();
            if(bitmap!=null&&!bitmap.isRecycled()){
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean compressImageToFile(File inFile,File outFile
            ,int reqWidth,int reqHeight) {
        return compressImageToFile(inFile,outFile,reqWidth,reqHeight,60);
    }

    public static boolean compressImageToFile(File inFile,File outFile
            ,int reqWidth,int reqHeight
            ,int quality) {
        boolean flag=false;
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(inFile.getAbsolutePath(),options);
            options.inSampleSize=calculateInSampleSize(options.outWidth,options.outHeight,reqWidth,reqHeight);
            options.inJustDecodeBounds=false;
            options.inPreferredConfig= Bitmap.Config.RGB_565;
            Bitmap bitmap=BitmapFactory.decodeFile(inFile.getAbsolutePath(),options);

            FileOutputStream fos=new FileOutputStream(outFile);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)){
                flag=true;
            }
            fos.flush();
            fos.close();
            if(bitmap!=null&&!bitmap.isRecycled()){
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static int calculateInSampleSize(int srcWidth, int srcHeight ,int targetWidth, int targetHeight) {
        float scale= Math.max(srcWidth *1f / targetWidth, srcHeight * 1f / targetHeight);
        int inSampleSize = Math.round(scale);
        if(inSampleSize<1){
            inSampleSize=1;
        }
        return inSampleSize;
    }

    public static Bitmap rotaingImageView(int angle,Bitmap bitmap) {
        Matrix matrix = new Matrix();

        // 旋转图片 动作
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//		bitmap.recycle();
        return resizedBitmap;
    }

    public static void notifyGallery(Context context, String filePath) throws URISyntaxException {
        if(context==null&& TextUtils.isEmpty(filePath)){
            return;
        }
        File f = new File(filePath);
        if(f.exists()){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        }
    }

}
