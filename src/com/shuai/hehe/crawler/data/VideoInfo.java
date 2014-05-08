package com.shuai.hehe.crawler.data;

/**
 *
 */
public class VideoInfo {
	/**
	 * 视频名
	 */
	public String mTitle;
	
	/**
	 * 视频预览图url
	 */
	public String mVideoThumbUrl;
	
	/**
	 * 视频url
	 */
	public String mVideoUrl;
	
	/**
     * 视频对应的web页面，该页面不仅包含视频还包含评论，广告等其它东西
     */
    public String mWebVideoUrl;
	
	/**
	 * 新鲜事来源
	 */
	public int mFromType=FromType.FROM_RENREN;

}
