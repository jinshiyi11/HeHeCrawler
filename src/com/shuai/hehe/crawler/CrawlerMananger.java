package com.shuai.hehe.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.shuai.hehe.crawler.data.AlbumInfo;
import com.shuai.hehe.crawler.data.DataManager;
import com.shuai.hehe.crawler.data.VideoInfo;

public class CrawlerMananger {
    private static CrawlerMananger mCrawlerMananger;
    
    private Executor mExecutor;
    
    private LinkedBlockingQueue<AlbumInfo> mAlbumInfos=new LinkedBlockingQueue<AlbumInfo>();
    private LinkedBlockingQueue<VideoInfo> mVideoInfos=new LinkedBlockingQueue<VideoInfo>();

    private CrawlerMananger() {
        mExecutor=new ThreadPoolExecutor(3, 6, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));
        
        mExecutor.execute(new Runnable() {
            
            @Override
            public void run() {
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
    
    public Executor getExecutor() {
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
            
            try {
                VideoInfo videoInfo=mVideoInfos.take();
                
                DataManager.getInstance().addHotVideo(videoInfo);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
