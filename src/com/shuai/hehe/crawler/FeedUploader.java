package com.shuai.hehe.crawler;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

/**
 * 将爬到的数据传到服务器上
 */
public class FeedUploader {
    private static final String URL_ADD_FEED="http://hehedream.duapp.com/add_feed";

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
		//return HttpClients.createDefault();
		HttpHost proxy = new HttpHost("localhost", 8888);
	    CloseableHttpClient client = HttpClients.custom().setProxy(proxy).build();
	    return client;
	}
	
	public void addFeed(Object info) {
		CloseableHttpClient httpClient = getDefaultHttpClient();

	    try {	        
	        HttpPost request = new HttpPost(URL_ADD_FEED);
	        Gson gson= new Gson();
	        StringEntity postingString  =new StringEntity(gson.toJson(info));
	        request.addHeader("content-type", "application/json; charset=utf8");
	        request.setEntity(postingString );
	        HttpResponse response = httpClient.execute(request);
	        StatusLine statusLine = response.getStatusLine();

	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    	ex.printStackTrace(System.err);
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
		
	}

}
