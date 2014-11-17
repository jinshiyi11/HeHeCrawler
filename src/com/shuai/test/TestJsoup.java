package com.shuai.test;

import com.shuai.hehe.crawler.BlogCrawler;
import com.shuai.hehe.crawler.CrawlerMananger;
import com.shuai.hehe.crawler.PicCrawler;
import com.shuai.hehe.crawler.VideoCrawler;

public class TestJsoup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	    //照片
		Thread picThread=new Thread(){

			@Override
			public void run() {
				PicCrawler picCrawler=new PicCrawler("http://share.renren.com/albumlist/0");
				picCrawler.start();
			}
			
		};
		
		picThread.setName("picThread");
		picThread.start();
		
		//视频
		Thread videoThread=new Thread(){

			@Override
			public void run() {
				VideoCrawler videoCrawler=new VideoCrawler("http://share.renren.com/videolist/0");
				videoCrawler.start();
			}
			
		};
		videoThread.setName("videoThread");
		videoThread.start();
		
		//日志
        Thread blogThread=new Thread(){

            @Override
            public void run() {
                BlogCrawler blogCrawler=new BlogCrawler("http://blog.renren.com/category/hot/1");
                blogCrawler.start();
            }
            
        };
        blogThread.setName("blogThread");
        blogThread.start();
		
		
		try {
		    picThread.join();
		    videoThread.join();
		    blogThread.join();
		    CrawlerMananger.getInstance().shutdown();
//			Thread.currentThread().wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
