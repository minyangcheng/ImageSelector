package com.min.album;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import com.min.album.activity.AlbumActivity;

import java.util.ArrayList;

/**
 * Created by minyangcheng on 2016/11/22.
 */

public class Album {

    private static AlbumConfig mConfig;

    public static void startAlbumForSingleChoice(Object obj){
        startAlbum(obj,false,-1,-1,null);
    }

    public static void startAlbumForMultiChoice(Object obj,int count,ArrayList<String> preSelectedList){
        startAlbum(obj,true,count,count,preSelectedList);
    }

    /**
     * 启动相册选择功能
     * @param obj activity或fragment对象
     * @param isMulti
     * @param maxCount
     * @param preSelectedList
     */
    public static void startAlbum(Object obj,boolean isMulti,int minCount,int maxCount,ArrayList<String> preSelectedList){
        checkConfig();
        if (obj==null) return;

        Activity activity=null;
        Fragment fragment=null;
        Context context=null;
        if(obj instanceof Activity){
            activity= (Activity) obj;
            context= activity;
        }else if(obj instanceof Fragment){
            fragment= (Fragment) obj;
            context=fragment.getActivity();
        }
        if(context==null) return;

        Intent intent=new Intent(context, AlbumActivity.class);
        if(isMulti){
            intent.putExtra(AlbumContant.KEY_IS_MULTI,isMulti);
            if(minCount>maxCount){
                maxCount=minCount;
            }
            if(maxCount<-1){
                maxCount=AlbumContant.DEFAULT_MAX_COUNT;
            }
            intent.putExtra(AlbumContant.KEY_MIN_COUNT,minCount);
            intent.putExtra(AlbumContant.KEY_MAX_COUNT,maxCount);
        }else{
            intent.putExtra(AlbumContant.KEY_IS_MULTI,false);
            intent.putExtra(AlbumContant.KEY_MAX_COUNT,1);
            intent.putExtra(AlbumContant.KEY_MIN_COUNT,1);
        }
        intent.putExtra(AlbumContant.KEY_PRE_SELECTED,preSelectedList);

        if(activity!=null){
            activity.startActivityForResult(intent, AlbumContant.ALBUM_REQUEST_CODE);
        }else{
            fragment.startActivityForResult(intent, AlbumContant.ALBUM_REQUEST_CODE);
        }
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data, OnResultListener listener){
        checkConfig();
        if(listener==null) return;
        if(requestCode==AlbumContant.ALBUM_REQUEST_CODE){
            if(resultCode==Activity.RESULT_OK){
                ArrayList<String> resultList= (ArrayList<String>) data.getSerializableExtra(AlbumContant.KEY_RESULT);
                listener.onResult(resultList);
            }
        }
    }

    public static AlbumImageLoader getImageLoader() {
        return mConfig.getImageLoader();
    }

    public static int getColumnNum(){
        return mConfig.getColumnNum();
    }

    public static void setConfig(AlbumConfig config){
        if(config!=null){
            mConfig=config;
        }
    }

    public static void checkConfig(){
        if(mConfig==null){
            throw new RuntimeException("Album config is null");
        }
        if(mConfig.getImageLoader()==null){
            throw new RuntimeException("Album imageloader is null");
        }
    }

    public interface OnResultListener{
        void onResult(ArrayList<String> resultList);
    }

}
