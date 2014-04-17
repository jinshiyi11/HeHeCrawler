package com.shuai.hehe.crawler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.google.gson.Gson;
import com.shuai.hehe.crawler.data.AlbumInfo.PicInfo;

public class DataManager {
	
	private static DataManager mDataManager;
	
	private static String[] FEED_TABLES={"hot_feed","hot_album","hot_video"};
	
	private boolean debug=true;
	
	private String mDbName;
	private String mDbUserName;
	private String mDbPassword;
	private String mDbHost;
	private int mDbPort;
	
	private static String mDriverName="com.mysql.jdbc.Driver";//"org.sqlite.JDBC"
	
	{
		if (debug) {
			mDbName = "hehe";
			mDbUserName="hot_feed_user";
			mDbPassword="test";
			mDbHost="localhost";
			mDbPort=3306;
		}else{
		    
		}
		
	}
	
	private DataManager(){
		try {
			Class.forName(mDriverName);
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
					"CREATE TABLE IF NOT EXISTS hot_feed(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
							"type INT,title VARCHAR(255) NOT NULL UNIQUE," +
							"content TEXT," +
							"`from` INT," +
							"state INT DEFAULT -1," +
							"insert_time TIMESTAMP DEFAULT  CURRENT_TIMESTAMP()," +
							"show_time TIMESTAMP DEFAULT  0" +
							")",
							
					"CREATE TABLE IF NOT EXISTS pic(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
							"feed_id INT," +
							"thumb_url TEXT," +
							"big_url TEXT," +
							"description TEXT," +
							"insert_time TIMESTAMP DEFAULT  CURRENT_TIMESTAMP()," +
							"show_time TIMESTAMP DEFAULT 0" +
							")"
							};
			
//			String[] indexs={"CREATE INDEX IF NOT EXISTS type_title_index ON hot_feed(type,title)"};
			
			statement = connection.createStatement();
			//创建表
			for(String sql:sqls){
				statement.execute(sql);				
			}
			
//			创建索引
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
	
	public static synchronized  DataManager getInstance(){
		if(mDataManager==null)
			mDataManager=new DataManager();
		return mDataManager;
	}
	
	private Connection getConnection() throws SQLException{
		//Connection connection = DriverManager.getConnection("jdbc:sqlite:D:/mycode/hehe_crawler.db");
	
		//jdbc:mysql://localhost:3306/dbname?user=sqluser&password=sqluserpw
		String connectionString=String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", mDbHost,mDbPort,mDbName,mDbUserName,mDbPassword);
		Connection connection = DriverManager.getConnection(connectionString);
		
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
	
	//TODO:除了单引号，还有其它字符也应该处理. http://stackoverflow.com/questions/881194/how-to-escape-special-character-in-mysql
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
		if(isFeedExist("hot_feed",info.mTitle))
			return;
		
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			
			VideoContent videoContent=new VideoContent();
			videoContent.videoUrl=info.mVideoUrl;
			videoContent.thumbImgUrl=info.mVideoThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(videoContent);
			
			statement=connection.prepareStatement("INSERT INTO hot_feed(type,title,content,`from`,show_time) values(?,?,?,?,?)");
			statement.setInt(1, FeedType.TYPE_VIDEO);
			statement.setString(2, processStringForSqlite(info.mTitle));
			statement.setString(3, processStringForSqlite(content));
			statement.setInt(4, info.mFromType);
			statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		
        try{
        synchronized (this) {
            
            notify();
        }
            }catch(Exception e){
                e.printStackTrace();
            }
        

        try {
            synchronized (this) {
                wait();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	/**
	 * 添加一个相册信息
	 * @param info
	 */
	public void addHotAlbum(AlbumInfo info){
		if(isFeedExist("hot_feed",info.mTitle))
			return;
		
		Connection connection = null;
		ResultSet generatedKeys = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			
			AlbumContent albumContent=new AlbumContent();
			albumContent.thumbImgUrl=info.mAlbumThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(albumContent);
			
			statement=connection.prepareStatement("INSERT INTO hot_feed(type,title,content,`from`,show_time) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, FeedType.TYPE_ALBUM);
			statement.setString(2, processStringForSqlite(info.mTitle));
			statement.setString(3, processStringForSqlite(content));
			statement.setInt(4, info.mFromType);
			statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			statement.executeUpdate();
			
			generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next()){
				long feed_id=generatedKeys.getLong(1);
				
				for (PicInfo pic : info.mPics) {
					statement=connection.prepareStatement("INSERT INTO pic(feed_id,thumb_url,big_url,description) values(?,?,?,?)");
					statement.setInt(1, (int) feed_id);
					statement.setString(2, processStringForSqlite(pic.mThumbUrl));
					statement.setString(3, processStringForSqlite(pic.mBigUrl));
					statement.setString(4, processStringForSqlite(pic.mDescription));
					statement.executeUpdate();
				}
			}else{
				throw new SQLException("INSERT INTO hot_feed failed!!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		
		try{
		synchronized (this) {
            
            notify();
		}
            }catch(Exception e){
                e.printStackTrace();
            }
        

        try {
            synchronized (this) {
                wait();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
			
		
	}

}
