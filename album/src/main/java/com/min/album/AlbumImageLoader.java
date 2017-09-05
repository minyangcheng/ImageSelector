package com.min.album;

import android.widget.ImageView;

/**
 * Created by minyangcheng on 2016/11/23.
 */

public interface AlbumImageLoader {

    void loadImage(String imagePath, ImageView imageView ,int expectWidht,int expectHeight);

}
