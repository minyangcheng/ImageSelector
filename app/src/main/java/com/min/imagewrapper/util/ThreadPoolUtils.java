package com.min.imagewrapper.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by minyangcheng on 2016/8/25.
 */
public class ThreadPoolUtils {

    public static ExecutorService threadPool= Executors.newFixedThreadPool(3);

}
