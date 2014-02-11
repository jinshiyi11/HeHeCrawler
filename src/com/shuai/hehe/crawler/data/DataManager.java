package com.shuai.hehe.crawler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DataManager {
	
	private static DataManager mDataManager;
	private Connection mConnection;
	
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
					"CREATE TABLE IF NOT EXISTS hot_feed(id INTEGER PRIMARY KEY,type INTEGER,title TEXT,content TEXT,[from] INTEGER,insert_time DATETIME DEFAULT (datetime('now','localtime')))",
					"CREATE TABLE IF NOT EXISTS hot_feed(id INTEGER PRIMARY KEY,feed_id INTEGER,thumb_url TEXT,big_url TEXT,description TEXT,insert_time DATETIME)"};
			
			String[] indexs={"CREATE INDEX IF NOT EXISTS type_title_index ON hot_feed(type,title)"};
			
			Statement statement = connection.createStatement();
			//创建表
			for(String sql:sqls){
				statement.execute(sql);				
			}
			
			//创建索引
			for(String sql:indexs){
				statement.execute(sql);				
			}
			
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
	
	public void addHotVideo(VideoInfo info) {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			
			VideoContent videoContent=new VideoContent();
			videoContent.videoUrl=info.mVideoUrl;
			videoContent.thumbImgUrl=info.mVideoThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(videoContent);
			
			
			String sql=String.format("INSERT INTO hot_feed(type,title,content,[from]) values(%d,'%s','%s',%d)",FeedType.TYPE_VIDEO,info.mTitle,content,FromType.FROM_RENREN);
			statement.execute(sql);
			statement.close();
			closeConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
