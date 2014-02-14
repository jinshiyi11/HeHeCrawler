package com.shuai.hehe.crawler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shuai.hehe.crawler.data.AlbumInfo.PicInfo;

public class DataManager {
	
	private static DataManager mDataManager;
	private Connection mConnection;
	
	private static String[] FEED_TABLES={"hot_feed","hot_album","hot_video"};
	
	private DataManager(){
//		try {
//			Class.forName("org.sqlite.JDBC");
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new RuntimeException();
//		}
		createDb();
	}
	
	/**
	 * 创建数据库
	 */
	private void createDb(){
		try {
			Connection connection=getConnection();
			
			String[] sqls={
					"CREATE TABLE IF NOT EXISTS hot_album(id INTEGER PRIMARY KEY," +
							"type INTEGER,title TEXT UNIQUE NOT NULL," +
							"content TEXT,[from] INTEGER," +
							"insert_time DATETIME DEFAULT (datetime('now','localtime')))",
							
					"CREATE TABLE IF NOT EXISTS hot_video(id INTEGER PRIMARY KEY," +
							"type INTEGER,title TEXT UNIQUE NOT NULL," +
							"content TEXT,[from] INTEGER," +
							"insert_time DATETIME DEFAULT (datetime('now','localtime')))",
							
					"CREATE TABLE IF NOT EXISTS pic(id INTEGER PRIMARY KEY,feed_id INTEGER,thumb_url TEXT,big_url TEXT,description TEXT,insert_time DATETIME DEFAULT (datetime('now','localtime')))"};
			
//			String[] indexs={"CREATE INDEX IF NOT EXISTS type_title_index ON hot_feed(type,title)"};
			
			Statement statement = connection.createStatement();
			//创建表
			for(String sql:sqls){
				statement.execute(sql);				
			}
			
			//创建索引
//			for(String sql:indexs){
//				statement.execute(sql);				
//			}
			
			closeConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DataManager getInstance(){
		if(mDataManager==null)
			mDataManager=new DataManager();
		return mDataManager;
	}
	
	private Connection getConnection() throws SQLException{
		if(mConnection!=null)
			return mConnection;
		
		mConnection = DriverManager.getConnection("jdbc:sqlite:D:/mycode/hehe.db");
		
		return mConnection;
	}
	
	private void closeConnection(){
		try {
			if (mConnection != null){
				mConnection.close();
				mConnection=null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class VideoContent{
		String videoUrl;
		String thumbImgUrl;
	}
	
	private static class AlbumContent{
		String thumbImgUrl;
	}
	
	public void addHotVideo(VideoInfo info) {
		Statement statement = null;
		try {
			Connection connection = getConnection();
			statement = connection.createStatement();
			
			VideoContent videoContent=new VideoContent();
			videoContent.videoUrl=info.mVideoUrl;
			videoContent.thumbImgUrl=info.mVideoThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(videoContent);
			
			
			String sql=String.format("INSERT INTO hot_video(type,title,content,[from]) values(%d,'%s','%s',%d)",FeedType.TYPE_VIDEO,info.mTitle,content,FromType.FROM_RENREN);
			statement.execute(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection();
		}
		
	}
	
	public void addHotAlbum(AlbumInfo info){
		ResultSet generatedKeys = null;
		Statement statement = null;
		try {
			Connection connection = getConnection();
			statement = connection.createStatement();
			
			AlbumContent albumContent=new AlbumContent();
			albumContent.thumbImgUrl=info.mAlbumThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(albumContent);
			
			
			String sql=String.format("INSERT INTO hot_album(type,title,content,[from]) values(%d,'%s','%s',%d)",FeedType.TYPE_ALBUM,info.mTitle,content,FromType.FROM_RENREN);
			statement.execute(sql);
			
			generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next()){
				long feed_id=generatedKeys.getLong(1);
				
				for (PicInfo pic : info.mPics) {
					sql=String.format("INSERT INTO pic(feed_id,thumb_url,big_url,description) values(%d,'%s','%s','%s')",feed_id,pic.mThumbUrl,pic.mBigUrl,pic.mDescription);
					statement.execute(sql);
				}
			}else{
				throw new SQLException("INSERT INTO hot_album failed!!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection();
		}
		
	}

}
