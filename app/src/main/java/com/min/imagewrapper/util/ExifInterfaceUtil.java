package com.min.imagewrapper.util;

import android.location.Location;
import android.media.ExifInterface;

/**
 * Created by minyangcheng on 2016/11/20.
 * 经纬度
 */

public class ExifInterfaceUtil {

    public static void setLatitudeAndLongtitude(ExifInterface exif , double latitude ,double longtitude){
        // 写入经度信息
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,gpsInfoConvert(longtitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,longtitude> 0 ? "E": "W");
        //写入纬度信息
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsInfoConvert(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude> 0 ? "N": "S");
    }

    public static float[] getLatitudeAndLongtitude(ExifInterface exif){
        float[] floatArr=new float[2];
        if(exif.getLatLong(floatArr)){
            return floatArr;
        }else{
            return null;
        }
    }

    private static String gpsInfoConvert(double gpsInfo) {
        gpsInfo= Math.abs(gpsInfo);
        String dms = Location.convert(gpsInfo, Location.FORMAT_SECONDS);
        String[]splits = dms.split(":");
        String[]secnds = (splits[2]).split("\\.");
        String seconds;
        if (secnds.length == 0) {
            seconds= splits[2];
        }else{
            seconds= secnds[0];
        }
        return splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
    }

}
