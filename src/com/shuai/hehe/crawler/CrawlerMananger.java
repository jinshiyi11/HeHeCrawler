package com.shuai.hehe.crawler;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.shuai.hehe.crawler.data.AlbumInfo;
import com.shuai.hehe.crawler.data.BlogInfo;
import com.shuai.hehe.crawler.data.DataManager;
import com.shuai.hehe.crawler.data.FeedType;
import com.shuai.hehe.crawler.data.VideoInfo;

public class CrawlerMananger {
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAXIMUM_POOL_SIZE = 5;//6;
    private static final int KEEP_ALIVE = 60;

    private static CrawlerMananger mCrawlerMananger;

    private ExecutorService mExecutor;
    private Random mRandom = new Random();

    private LinkedBlockingQueue<AlbumInfo> mAlbumInfos = new LinkedBlockingQueue<AlbumInfo>();
    private LinkedBlockingQueue<VideoInfo> mVideoInfos = new LinkedBlockingQueue<VideoInfo>();
    private LinkedBlockingQueue<BlogInfo> mBlogInfos = new LinkedBlockingQueue<BlogInfo>();

    private CrawlerMananger() {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));

        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName("DB Thread");
                processFeed();
            }
        });
    }

    public static synchronized CrawlerMananger getInstance() {
        if (mCrawlerMananger == null) {
            mCrawlerMananger = new CrawlerMananger();
        }

        return mCrawlerMananger;
    }

    public void shutdown() {
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Executor getExecutor() {
        int queueSize = ((ThreadPoolExecutor) mExecutor).getQueue().size();
        System.out.println("CrawlerMananger queue size:" + queueSize);
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

    public void addVideo(VideoInfo info) {
        try {
            mVideoInfos.put(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addBlog(BlogInfo info) {
        try {
            mBlogInfos.put(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据预定的比例获取应该插入的feed类型
     * 
     * @return
     */
    private int chooseFeed() {
        int result;
        int num = mRandom.nextInt(10) + 1;
        if (num <= 7) {
            result = FeedType.TYPE_ALBUM;
        } else{
            result = FeedType.TYPE_VIDEO;
        }
        
        /*else if (num <= 9) {
            result = FeedType.TYPE_VIDEO;
        } else {
            result = FeedType.TYPE_BLOG;
        }*/

        return result;
    }

    public void processFeed() {
        while (true) {
            try {
                int type = chooseFeed();
                //type=FeedType.TYPE_ALBUM;
                switch (type) {
                case FeedType.TYPE_ALBUM: {
                    AlbumInfo info = mAlbumInfos.poll(10, TimeUnit.MINUTES);
                    if (info != null) {
                        System.out.println(info);
                        FeedUploader.getInstance().addFeed(info);
                        //DataManager.getInstance().addAlbum(info);
                    }
                }
                    break;

                case FeedType.TYPE_VIDEO: {
                    VideoInfo info = mVideoInfos.poll(10, TimeUnit.MINUTES);
                    if (info != null) {
                        System.out.println(info);
                        FeedUploader.getInstance().addFeed(info);
                        //DataManager.getInstance().addVideo(info);
                    }
                }
                    break;
                    
                case FeedType.TYPE_BLOG: {
                    BlogInfo info = mBlogInfos.poll(10, TimeUnit.MINUTES);
                    if (info != null) {
                        System.out.println(info);
                        FeedUploader.getInstance().addFeed(info);
                        //DataManager.getInstance().addBlog(info);
                    }
                }
                    break;

                default:
                    break;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}
