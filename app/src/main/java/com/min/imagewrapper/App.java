package com.min.imagewrapper;

import android.app.Application;
import android.content.Context;
import android.widget.ImageView;

import com.min.album.Album;
import com.min.album.AlbumConfig;
import com.min.album.AlbumImageLoader;
import com.min.imagewrapper.util.ImageLoaderWrap;

import java.io.File;

/**
 * Created by minyangcheng on 2016/4/26.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        ImageLoaderWrap.init(this);
        initAlbumConfig();
    }

    private void initAlbumConfig() {
        AlbumConfig albumConfig=new AlbumConfig.Builder().setImageLoader(new AlbumImageLoader() {
            @Override
            public void loadImage(String imagePath, ImageView imageView,int expectWidht,int expectHeight) {
                ImageLoaderWrap.displayFileImage(new File(imagePath),imageView,expectWidht,expectHeight);
            }
        }).setColumnNum(3)
                .build();
        Album.setConfig(albumConfig);
    }

    public static Context getContext(){
        return context;
    }

}
