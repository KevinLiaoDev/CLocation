package top.kevinliaodev.clocation;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lk on 2016/12/14.
 * 自己的单线程池，防止sdk用asynTask阻塞线程池
 */
public abstract class MyAsynTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static ExecutorService photosThreadPool;//用于加载大图和评论的线程池
    public void executeDependSDK(Params...params){
        if(photosThreadPool==null)
            photosThreadPool = Executors.newSingleThreadExecutor();
        if(Build.VERSION.SDK_INT<11)
            super.execute(params);
        else
            super.executeOnExecutor(photosThreadPool,params);
    }

}
