package com.shuai.hehe.crawler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
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
	
	private long mShowTime=System.currentTimeMillis();
	public long mShowTimeStep=30*60*1000;
	
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
		
		//init startTime
		initShowTime();
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
							")",
							
					"CREATE TABLE IF NOT EXISTS blog(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "feed_id INT," +
                            "html_content TEXT," +
                            "insert_time TIMESTAMP DEFAULT  CURRENT_TIMESTAMP()" +
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
	
	/**
	 * 新插入的新鲜事的展示时间接在前一条新鲜事之后
	 * @throws SQLException
	 */
	private void initShowTime(){
	    Connection connection;
        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT show_time FROM hot_feed ORDER BY show_time DESC LIMIT 1");
            if(resultSet.next()){
                mShowTime=resultSet.getTimestamp("show_time").getTime()+mShowTimeStep;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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
	    /**
	     * flash格式视频的url
	     */
	    @SerializedName("flashVideoUrl")
		String mFlashVideoUrl;
	    
		/**
		 * 视频对应的web页面，该页面不仅包含视频还包含评论，广告等其它东西
		 */
	    @SerializedName("webVideoUrl")
		String mWebVideoUrl;
		
	    /**
	     * 视频预览图url
	     */
	    @SerializedName("videoThumbUrl")
		String mVideoThumbUrl;
	}
	
	private static class AlbumContent{
	    @SerializedName("thumbImgUrl")
		String mThumbImgUrl;
	    
	    @SerializedName("bigImgUrl")
		String mBigImgUrl;
	}
	
	private static class BlogContent{
	    /**
	     * 日志摘要
	     */
	    @SerializedName("summary")
	    String mSummary;
	    
	    /**
	     * 日志对应的url页面
	     */
	    @SerializedName("webUrl")
	    String mWebUrl;
	}
	
	public boolean isFeedExist(String title){
	    return isFeedExist("hot_feed", title);
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
	public void addVideo(VideoInfo info) {
		if(isFeedExist("hot_feed",info.mTitle))
			return;
		
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			
			VideoContent videoContent=new VideoContent();
			videoContent.mFlashVideoUrl=info.mFlashVideoUrl;
			videoContent.mWebVideoUrl=info.mWebVideoUrl;
			videoContent.mVideoThumbUrl=info.mVideoThumbUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(videoContent);
			
			statement=connection.prepareStatement("INSERT INTO hot_feed(type,title,content,`from`,show_time) values(?,?,?,?,?)");
			statement.setInt(1, FeedType.TYPE_VIDEO);
			statement.setString(2, info.mTitle);
			statement.setString(3, content);
			statement.setInt(4, info.mFromType);			
			statement.setTimestamp(5, new Timestamp(mShowTime));
			statement.executeUpdate();
			mShowTime+=mShowTimeStep;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}

	}
	
	/**
	 * 添加一个相册信息
	 * @param info
	 */
	public void addAlbum(AlbumInfo info){
		if(isFeedExist("hot_feed",info.mTitle))
			return;
		
		Connection connection = null;
		ResultSet generatedKeys = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			
			AlbumContent albumContent=new AlbumContent();
			albumContent.mThumbImgUrl=info.mAlbumThumbUrl;
			albumContent.mBigImgUrl=info.mAlbumPicUrl;
			
			Gson gson=new Gson();
			String content=gson.toJson(albumContent);
			
			statement=connection.prepareStatement("INSERT INTO hot_feed(type,title,content,`from`,show_time) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, FeedType.TYPE_ALBUM);
			statement.setString(2, info.mTitle);
			statement.setString(3, content);
			statement.setInt(4, info.mFromType);			
			statement.setTimestamp(5, new Timestamp(mShowTime));
			statement.executeUpdate();
			
			generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next()){
				long feed_id=generatedKeys.getLong(1);
				
				for (PicInfo pic : info.mPics) {
					statement=connection.prepareStatement("INSERT INTO pic(feed_id,thumb_url,big_url,description) values(?,?,?,?)");
					statement.setInt(1, (int) feed_id);
					statement.setString(2, pic.mThumbImgUrl);
					statement.setString(3, pic.mBigImgUrl);
					statement.setString(4, pic.mDescription);
					statement.executeUpdate();
				}
			}else{
				throw new SQLException("INSERT INTO hot_feed failed!!");
			}
			mShowTime+=mShowTimeStep;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
//			if (statement != null) try { statement.close(); } catch (SQLException logOrIgnore) {}
			closeConnection(connection);
		}
		
	}
	
	/**
	 * 添加一条日志
	 * @param info
	 */
	public void addBlog(BlogInfo info){
        if(isFeedExist("hot_feed",info.getTitle()))
            return;
        
        Connection connection = null;
        ResultSet generatedKeys = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            BlogContent contentData=new BlogContent();
            contentData.mSummary=info.getSummary();
            contentData.mWebUrl=info.getWebUrl();
            
            Gson gson=new Gson();
            String content=gson.toJson(contentData);
            
            statement=connection.prepareStatement("INSERT INTO hot_feed(type,title,content,`from`,show_time) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, FeedType.TYPE_BLOG);
            statement.setString(2, info.getTitle());
            statement.setString(3, content);
            statement.setInt(4, info.getFromType());            
            statement.setTimestamp(5, new Timestamp(mShowTime));
            statement.executeUpdate();
            
            generatedKeys = statement.getGeneratedKeys();
            if(generatedKeys.next()){
                long feed_id=generatedKeys.getLong(1);
                
                statement=connection.prepareStatement("INSERT INTO blog(feed_id,html_content) values(?,?)");
                statement.setInt(1, (int) feed_id);
                statement.setString(2, info.getHtmlContent());
                statement.executeUpdate();
            }else{
                throw new SQLException("INSERT INTO hot_feed failed!!");
            }
            mShowTime+=mShowTimeStep;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            closeConnection(connection);
        }
	}


	/**
	 * 辅助更新show_time字段
	 */
    public void updateShowTime() {
        long startTime=0;
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-06-19 09:03:31").getTime();
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Connection connection = getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("SELECT id, show_time FROM hehe.hot_feed where id>240",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = prepareStatement.executeQuery();
            while(rs.next()){
                rs.updateTimestamp(2, new Timestamp(startTime));
                startTime+=mShowTimeStep;
                rs.updateRow();
            }
            rs.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
