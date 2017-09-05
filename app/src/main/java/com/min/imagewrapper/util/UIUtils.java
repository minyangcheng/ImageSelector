package com.min.imagewrapper.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by minyangcheng on 2016/8/19.
 */
public class UIUtils {

    public static void toast(Context context , String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

}
