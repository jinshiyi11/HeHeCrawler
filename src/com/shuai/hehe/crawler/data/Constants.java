package com.shuai.hehe.crawler.data;

public final class Constants {
    
    public static String USER_AGENT="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";
    
	/**
	 * 爬虫访问网络的超时时间，单位ms
	 */
	public static final int JSOUP_TIMEOUT=30*1000;
	/**
	 * 新鲜事还未审核
	 */
	public static final int FEED_STATE_UNCHECK=-1;
	
	/**
	 * 该新鲜事不是优质内容，不展示给用户
	 */
	public static final int FEED_STATE_NORMAL=0;
	
	/**
	 * 该新鲜事是优质内容，展示给用户
	 */
	public static final int FEED_STATE_GOOD=1;

}
