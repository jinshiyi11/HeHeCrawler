package com.shuai.hehe.crawler.data;

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
	 * 相册封面缩略图url
	 */
	public String mAlbumThumbUrl;
	
	/**
     * 相册封面大图url
     */
    public String mAlbumPicUrl;
	
	/**
	 * 新鲜事来源
	 */
	public int mFromType=FromType.FROM_RENREN;
	
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
		public String mThumbImgUrl;
		
		/**
		 * 相片大图url
		 */
		public String mBigImgUrl;
		
		/**
		 * 相片描述
		 */
		public String mDescription;
	}

    @Override
    public String toString() {
        return "album=mTitle:"+mTitle+"\n";
    }

}
