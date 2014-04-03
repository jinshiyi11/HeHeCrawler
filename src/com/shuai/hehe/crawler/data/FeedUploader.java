package com.shuai.hehe.crawler.data;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

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
	}
	
	public void addHotVideo(VideoInfo info) {
		CloseableHttpClient httpClient = getDefaultHttpClient();

	    try {
	        HttpPost request = new HttpPost("http://yoururl");
	        Gson gson= new Gson();
	        StringEntity postingString  =new StringEntity(gson.toJson(info));
	        request.addHeader("content-type", "application/json; charset=utf8");
	        request.setEntity(postingString );
	        HttpResponse response = httpClient.execute(request);

	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    	ex.printStackTrace(System.err);
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
		
	}

}
