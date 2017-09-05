package com.min.imagewrapper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.min.imagewrapper.BuildConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

/**
 * Created by minyangcheng on 2016/6/1.
 */
public class ImageLoaderWrap {

    public static void init(Context context){
        ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY-1)
                .diskCacheSize(80 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO);
        if(BuildConfig.DEBUG){
            configBuilder.writeDebugLogs();
        }
        ImageLoaderConfiguration config=configBuilder.build();
        ImageLoader.getInstance().init(config);
    }

    public static void displayHttpImage(String url,ImageView imageView){
        displayImage(url,imageView,null,null);
    }

    public static void displayHttpImage(String url,ImageView imageView,int width,int height){
        ImageSize imageSize=null;
        if(width>0&&height>=0){
            imageSize=new ImageSize(width,height);
        }
        displayImage(url,imageView,null,imageSize);
    }

    public static void displayDrawableImage(int drawableId,ImageView imageView){
        String url="drawable://"+drawableId;
        displayImage(url,imageView,null,null);
    }

    public static void displayDrawableImage(int drawableId,ImageView imageView,int width,int height){
        ImageSize imageSize=null;
        if(width>0&&height>=0){
            imageSize=new ImageSize(width,height);
        }
        String url="drawable://"+drawableId;
        displayImage(url,imageView,null,imageSize);
    }

    public static void displayFileImage(File file, ImageView imageView){
        String filePath=(file==null)?"":file.getAbsolutePath();
        String url="file://"+filePath;
        displayImage(url,imageView,null,null);
    }

    public static void displayFileImage(File file, ImageView imageView,int width,int height){
        ImageSize imageSize=null;
        if(width>0&&height>=0){
            imageSize=new ImageSize(width,height);
        }
        String filePath=(file==null)?"":file.getAbsolutePath();
        String url="file://"+filePath;
        displayImage(url,imageView,null,imageSize);
    }

    private static void displayImage(String url, ImageView imageView, DisplayImageOptions displayImageOptions, ImageSize imageSize){
        ImageLoader.getInstance().displayImage(url, new ImageViewAware(imageView),
                displayImageOptions == null ? getDisplayImageOptions() : displayImageOptions,imageSize
                ,null,null);
    }

    public static void loadImage(String url, final ImageLoadListener loadListener){
        ImageLoader.getInstance().loadImage(url, getDisplayImageOptions(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (loadListener != null) {
                    loadListener.onLoadingStarted(imageUri, view);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (loadListener != null) {
                    loadListener.onLoadingFailed(imageUri, view);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadListener != null) {
                    loadListener.onLoadingComplete(imageUri, view, loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (loadListener != null) {
                    loadListener.onLoadingCancelled(imageUri, view);
                }
            }
        });
    }

    public static DisplayImageOptions getDisplayImageOptions(){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
//                .delayBeforeLoading(100)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();
        return options;
    }

    public interface ImageLoadListener {

        void onLoadingStarted(String imageUri, View view);

        void onLoadingFailed(String imageUri, View view);

        void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

        void onLoadingCancelled(String imageUri, View view);
    }

}
