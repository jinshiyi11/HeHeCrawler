package com.shuai.hehe.crawler;

import java.security.InvalidParameterException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.shuai.hehe.crawler.data.AlbumInfo;
import com.shuai.hehe.crawler.data.BlogInfo;
import com.shuai.hehe.crawler.data.Constants;
import com.shuai.hehe.crawler.data.FeedType;
import com.shuai.hehe.crawler.data.VideoInfo;

/**
 * 将爬到的数据传到服务器上
 */
public class FeedUploader {
	private static FeedUploader mSelf;

	private FeedUploader() {
	}

	public static synchronized FeedUploader getInstance() {
		if (mSelf == null) {
			mSelf = new FeedUploader();
		}
		return mSelf;
	}
	
	private CloseableHttpClient getDefaultHttpClient(){
		return HttpClients.createDefault();
//		HttpHost proxy = new HttpHost("localhost", 8888);
//	    CloseableHttpClient client = HttpClients.custom().setProxy(proxy).build();
//	    return client;
	}
	
	public void addFeed(Object info) {
		CloseableHttpClient httpClient = getDefaultHttpClient();

		int feedType=FeedType.TYPE_ALBUM;
		if(info.getClass()==AlbumInfo.class){
		    feedType=FeedType.TYPE_ALBUM;
		}else if(info.getClass()==VideoInfo.class){
		    feedType=FeedType.TYPE_VIDEO;
		}else if(info.getClass()==BlogInfo.class){
		    feedType=FeedType.TYPE_BLOG;
		}else{
		    throw new InvalidParameterException("feed type invalid!");
		}
		
	    try {	 
	        URIBuilder builder=new URIBuilder(Constants.URL_ADD_FEED);
	        builder.addParameter(Constants.ADMIN_KEY_NAME, Constants.ADMIN_KEY_VALUE);
	        builder.addParameter("feedtype",Integer.toString(feedType));
	        HttpPost request = new HttpPost(builder.build());
	        Gson gson= new Gson();
	        StringEntity postingString  =new StringEntity(gson.toJson(info),"UTF-8");
	        request.addHeader("content-type", "application/json; charset=utf8");
	        request.setEntity(postingString );
	        HttpResponse response = httpClient.execute(request);
	        StatusLine statusLine = response.getStatusLine();

	    }catch (Exception ex) {
	    	ex.printStackTrace(System.err);
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
		
	}

}
