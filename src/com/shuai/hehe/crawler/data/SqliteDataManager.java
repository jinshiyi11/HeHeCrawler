package com.shuai.hehe.crawler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.shuai.hehe.crawler.data.AlbumInfo.PicInfo;

public class SqliteDataManager {
	
	private static SqliteDataManager mDataManager;
	
	private static String[] FEED_TABLES={"hot_feed","hot_album","hot_video"};
	
	private SqliteDataManager(){
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
		createDb();
	}
	
	/**
	 * 创建数据库
	 */
	private void createDb(){
		Connection connection=null;
		Statement statement=null;
		try {
			connection=getConnection();
			
			String[] sqls={
					"CREATE TABLE IF NOT EXISTS hot_album(id INTEGER PRIMARY KEY," +
							"type INTEGER,title TEXT UNIQUE NOT NULL," +
							"content TEXT," +
							"[from] INTEGER," +
							"state INTEGER DEFAULT -1," +
							"insert_time INTEGER DEFAULT (strftime('%s', 'now'))," +
							"show_time INTEGER DEFAULT 0" +
							")",
							
					"CREATE TABLE IF NOT EXISTS hot_video(id INTEGER PRIMARY KEY," +
							"type INTEGER,title TEXT UNIQUE NOT NULL," +
							"content TEXT," +
							"[from] INTEGER," +
							"state INTEGER DEFAULT -1," +
							"insert_time INTEGER DEFAULT (strftime('%s', 'now'))," +
							"show_time INTEGER DEFAULT 0" +
							")",
							
					"CREATE TABLE IF NOT EXISTS pic(id INTEGER PRIMARY KEY," +
							"feed_id INTEGER," +
							"thumb_url TEXT," +
							"big_url TEXT," +
							"description TEXT," +
							"insert_time INTEGER DEFAULT (strftime('%s', 'now'))," +
							"show_time INTEGER DEFAULT 0" +
							")"
							};
			
//			String[] indexs={"CREATE INDEX IF NOT EXISTS type_title_index ON hot_feed(type,title)"};
			
			statement = connection.createStatement();
			//创建表
			for(String sql:sqls){
				statement.execute(sql);				
			}
			
			//创建索引
//			for(String sql:indexs){
//				statement.execute(sql);				
//			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
	}
	
	public static synchronized  SqliteDataManager getInstance(){
		if(mDataManager==null)
			mDataManager=new SqliteDataManager();
		return mDataManager;
	}
	
	private Connection getConnection() throws SQLException{
		Connection connection = DriverManager.getConnection("jdbc:sqlite:D:/mycode/hehe_crawler.db");
		
		return connection;
	}
	
	private void closeConnection(Connection connection){
		try {
			if (connection != null){
				connection.close();
//				connection=null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String processStringForSqlite(String string){
		//把单引号替换为2个单引号
		return string.replace("'", "''");
	}
	
	private static class VideoContent{
		String videoUrl;
		String thumbImgUrl;
	}
	
	private static class AlbumContent{
		String thumbImgUrl;
	}
	
	/**
	 * 检查新鲜事是否已存在
	 * @tableName 表名
	 * @title 新鲜事标题
	 * @return
	 */
	public synchronized boolean isFeedExist(String tableName,String title){
		boolean exist=false;
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet=null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery(String.format("select * from %s where title='%s' limit 1", tableName,processStringForSqlite(title)));
			if(resultSet.next())
				exist=true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (resultSet != null) try { resultSet.close(); } catch (SQLException logOrIgnore) {}
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		return exist;
	}
	
	/**
	 * 添加热门视频
	 * @param info
	 */
	public void addHotVideo(VideoInfo info) {
		if(isFeedExist("hot_video",info.mTitle))
			return;
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			
			VideoContent videoContent=new VideoContent();
			videoContent.videoUrl=info.mFlashVideoUrl;
			videoContent.thumbImgUrl=info.mVideoThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(videoContent);
			
			
			String sql=String.format("INSERT INTO hot_video(type,title,content,[from]) values(%d,'%s','%s',%d)",FeedType.TYPE_VIDEO,processStringForSqlite(info.mTitle),processStringForSqlite(content),FromType.FROM_RENREN);
			statement.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		
	}
	
	public void addHotAlbum(AlbumInfo info){
		if(isFeedExist("hot_album",info.mTitle))
			return;
		
		Connection connection = null;
		ResultSet generatedKeys = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			
			AlbumContent albumContent=new AlbumContent();
			albumContent.thumbImgUrl=info.mAlbumThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(albumContent);
			
			
			String sql=String.format("INSERT INTO hot_album(type,title,content,[from]) values(%d,'%s','%s',%d)",FeedType.TYPE_ALBUM,processStringForSqlite(info.mTitle),processStringForSqlite(content),FromType.FROM_RENREN);
			statement.execute(sql);
			
			generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next()){
				long feed_id=generatedKeys.getLong(1);
				
				for (PicInfo pic : info.mPics) {
					sql=String.format("INSERT INTO pic(feed_id,thumb_url,big_url,description) values(%d,'%s','%s','%s')",feed_id,processStringForSqlite(pic.mThumbImgUrl),processStringForSqlite(pic.mBigImgUrl),processStringForSqlite(pic.mDescription));
					statement.execute(sql);
				}
			}else{
				throw new SQLException("INSERT INTO hot_album failed!!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		
	}

}
