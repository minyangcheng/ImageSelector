package com.min.imagewrapper.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class FilePathUtil {

    /**
     * getExternalStorageDirectory=/storage/sdcard0
     getExternalFilesDir=/storage/sdcard0/Android/data/com.slh.download/files
     getExternalFilesDirMusic=/storage/sdcard0/Android/data/com.slh.download/files/Music
     getExternalCacheDir=/storage/sdcard0/Android/data/com.slh.download/cache
     getFileDir=/data/data/com.slh.download/files
     getCacheDir=/data/data/com.slh.download/cache
     * @param context
     * @return
     */
    public static File getFilesRootDir(Context context){
        File dir=context.getExternalFilesDir(null);
        if(dir==null){
            dir=context.getFilesDir();
        }
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
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

    public static File getSharedRootDir(String name){
        if(TextUtils.isEmpty(name)){
            return null;
        }
        File fileDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            fileDir=new File(Environment.getExternalStorageDirectory(),name);
        }else {
            fileDir=new File("/"+name);
        }
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        return fileDir;
    }

}
