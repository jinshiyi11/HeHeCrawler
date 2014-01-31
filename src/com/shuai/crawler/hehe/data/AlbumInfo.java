package com.shuai.crawler.hehe.data;

import java.util.ArrayList;

/**
 * 相册信息
 */
public class AlbumInfo {
	/**
	 * 相册名
	 */
	public String mTitle;
	
	/**
	 * 相册缩略图url
	 */
	public String mAlbumThumbUrl;
	
	/**
	 * 相册里面的相片
	 */
	public ArrayList<PicInfo> mPics=new ArrayList<AlbumInfo.PicInfo>();
	
	/**
	 * 每个相片的信息
	 */
	public static class PicInfo{
		/**
		 * 相片缩略图url
		 */
		public String mThumbUrl;
		
		/**
		 * 相片大图url
		 */
		public String mBigUrl;
		
		/**
		 * 相片描述
		 */
		public String mDescription;
	}

}
