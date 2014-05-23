package com.shuai.hehe.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.shuai.hehe.crawler.data.AlbumInfo;
import com.shuai.hehe.crawler.data.DataManager;
import com.shuai.hehe.crawler.data.VideoInfo;

public class CrawlerMananger {
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAXIMUM_POOL_SIZE = 5;//6;
    private static final int KEEP_ALIVE = 5;
    
    private static CrawlerMananger mCrawlerMananger;
    
    private ExecutorService mExecutor;
    
    private LinkedBlockingQueue<AlbumInfo> mAlbumInfos=new LinkedBlockingQueue<AlbumInfo>();
    private LinkedBlockingQueue<VideoInfo> mVideoInfos=new LinkedBlockingQueue<VideoInfo>();

    private CrawlerMananger() {
        mExecutor=new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
        
        mExecutor.execute(new Runnable() {
            
            @Override
            public void run() {
                Thread.currentThread().setName("DB Thread");
                processFeed();
            }
        });
    }
    
    public static synchronized CrawlerMananger getInstance() {
        if(mCrawlerMananger==null){
            mCrawlerMananger=new CrawlerMananger();
        }
        
        return mCrawlerMananger;
    }
    
    public void shutdown(){
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public Executor getExecutor() {
        int queueSize=((ThreadPoolExecutor)mExecutor).getQueue().size();
        System.out.println("CrawlerMananger queue size:"+queueSize);
        if (queueSize > 500) {
            try {
                Thread.currentThread().sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return mExecutor;
    }
    
    public void addAlbum(AlbumInfo info) {
        try {
            mAlbumInfos.put(info);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void addVideo(VideoInfo info){
        try {
            mVideoInfos.put(info);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void processFeed() {
        while (true) {
            try {
                AlbumInfo albumInfo=mAlbumInfos.take();
                
                DataManager.getInstance().addHotAlbum(albumInfo);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /* 
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            try {
                VideoInfo videoInfo=mVideoInfos.take();
                
                DataManager.getInstance().addHotVideo(videoInfo);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            */
        }
    }

}
