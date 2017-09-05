package com.min.imagewrapper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Created by minyangcheng on 2016/9/23.
 */
public class GsonUtil {

    public static Gson gson=new Gson();

    public static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static  <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    /**
     * 日志打印美化
     * @param src
     * @return
     */
    public static String toPrettyJson(Object src) {
        if(src==null) return null;
        return prettyGson.toJson(src);
    }

}
