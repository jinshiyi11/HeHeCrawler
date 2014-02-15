package com.shuai.test;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.shuai.hehe.crawler.PicCrawler;
import com.shuai.hehe.crawler.VideoCrawler;

public class TestJsoup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		getHotPics("http://share.renren.com/albumlist/0",0,0);
		
//		PicCrawler picCrawler=new PicCrawler("http://share.renren.com/albumlist/0");
//		picCrawler.start();
		
		Thread picThread=new Thread(){

			@Override
			public void run() {
				PicCrawler picCrawler=new PicCrawler("http://share.renren.com/albumlist/0");
				picCrawler.start();
			}
			
		};
		
		picThread.setName("picThread");
		picThread.start();
		
		Thread videoThread=new Thread(){

			@Override
			public void run() {
				VideoCrawler videoCrawler=new VideoCrawler("http://share.renren.com/videolist/0");
				videoCrawler.start();
			}
			
		};
		
		videoThread.setName("videoThread");
		videoThread.start();
		
		
		try {
			videoThread.join();
//			Thread.currentThread().wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void getHotPics(String url,Integer count,int stackDeep){
		if(url==null)
			return;
		url=url.trim();
		if(url.isEmpty())
			return;
		
		if(stackDeep>10)
			return;
		
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			//System.out.print(doc);
			Elements links = doc.select(".share-hot-list h3 a[href]");
			System.out.println("pagecount:"+links.size());
			for(Element element:links){
				String title=element.ownText();
				String href=element.attr("href");
				if(href.startsWith("http://share.renren.com/")){
					System.out.println(title);
					System.out.println(href);
					System.out.println("count:"+ ++count);
				}
			}
			
			Elements elements=doc.select(".pagerpro a[title=下一页]");
			//check element count,==1.if >1  ?
			if(elements.size()>0){
				Element nextElement=elements.get(0);
				String nextPageUrl=nextElement.attr("href");
				
				System.out.println();
				System.out.println();
				getHotPics(nextPageUrl,count,++stackDeep);				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getLinks(){
		Document doc;
		try {
			doc = Jsoup.connect("http://blog.renren.com/").get();
			//System.out.print(doc);
			Elements links = doc.select("a[title]");
			System.out.println("count:"+links.size());
			for(Element element:links){
				String title=element.attr("title");
				String href=element.attr("href");
				if(href.startsWith("http://blog.renren.com/share")){
					System.out.println(title);
					System.out.println(href);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
