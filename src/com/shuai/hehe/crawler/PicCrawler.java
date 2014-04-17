package com.shuai.hehe.crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.shuai.hehe.crawler.data.AlbumInfo;
import com.shuai.hehe.crawler.data.AlbumInfo.PicInfo;
import com.shuai.hehe.crawler.data.Constants;
import com.shuai.hehe.crawler.data.FromType;

/**
 * 热门相册爬虫
 */
public class PicCrawler {

	/**
	 * 爬虫的起始url
	 */
	private String mStartUrl;

	/**
	 * 已经爬过的相册数
	 */
	private int mAlbumCount;

	/**
	 * 已经爬过的页面数
	 */
	private int mPageCount;
	
	private static int MAX_ALBUM_COUNT=1;
	
	private CrawlerMananger mCrawlerMananger=CrawlerMananger.getInstance();
	
    private class SubPicCrawler implements Runnable {
        String title;
        String href;
        String thumbUrl;

        public SubPicCrawler(String href, String title, String thumbUrl) {
            this.href = href;
            this.title = title;
            this.thumbUrl = thumbUrl;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("SubPicCrawler");
            AlbumInfo albumInfo = getAlbumInfo(href, title, thumbUrl);
            mCrawlerMananger.addAlbum(albumInfo);
        }
    }

	public PicCrawler(String startUrl) {
		mStartUrl = startUrl;
	}

	public void start() {
	    String url=mStartUrl;
        while (url!=null && url.length()>0) {
            url=getAlbums(url);
        }
	}

	/**
	 * 获取该页面包含的相册
	 * @param url 从该页面爬取相册
	 */
	private String getAlbums(String url) {
		if (url == null)
			new IllegalArgumentException();

		url = url.trim();
		if (url.isEmpty())
			throw new IllegalArgumentException();
		
		System.out.println(String.format("正在爬取相册列表，url:%s",url));

		++mPageCount;

//		if (mPageCount > 10)
//			return;

		Document doc;
		try {
			doc = Jsoup.connect(url).timeout(Constants.JSOUP_TIMEOUT).get();
			// System.out.print(doc);
			Elements items = doc.select(".share-hot-list .share.clearfix");
			
			System.out.println("pagecount:" + items.size());
            for (Element element : items) {
                Element link = element.select("h3 a[href]").get(0);
                //TODO:check
                String title = link.ownText();
                String href = link.attr("href");

                Element img = element.select(".content .photos img").get(0);
                String thumbUrl = img.attr("src");
                if (href.startsWith("http://share.renren.com/")) {
                    System.out.println(title);
                    System.out.println(href);
                    System.out.println("count:" + ++mAlbumCount);
                    
                    mCrawlerMananger.getExecutor().execute(new SubPicCrawler(href, title, thumbUrl));
                }

            }
			

			Elements elements = doc.select(".pagerpro a[title=下一页]");
			// check element count,==1.if >1 ?
			if (elements.size() > 0) {
				Element nextPageElement = elements.get(0);
				String nextPageUrl = nextPageElement.attr("href");

				// System.out.println();
				// System.out.println();

				return nextPageUrl;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取相册信息
	 * @param albumUrl
	 * @param albumTitle
	 * @param albumThumbUrl
	 */
	private AlbumInfo getAlbumInfo(String albumUrl, String albumTitle,String albumThumbUrl){
        AlbumInfo albumInfo = new AlbumInfo();
        albumInfo.mTitle = albumTitle;
        albumInfo.mAlbumThumbUrl = albumThumbUrl;
        albumInfo.mFromType = FromType.FROM_RENREN;

        System.out.print(String.format("正在爬取相册图片\n相册名：%s\nurl:%s\n缩略图:%s\n", albumTitle, albumUrl, albumThumbUrl));
        getAlbumPics(albumUrl, albumInfo);

        System.out.println(String.format("相册:%s包含%d张图片", albumTitle, albumInfo.mPics.size()));
        return albumInfo;
	}

	/**
	 * 获取该相册的图片
	 * @param albumUrl 相册url
	 * @param albumTitle 相册名
	 */
	private void getAlbumPics(String albumUrl, AlbumInfo albumInfo) {
		if (albumUrl == null)
			new IllegalArgumentException();

		albumUrl = albumUrl.trim();
		if (albumUrl.isEmpty())
			throw new IllegalArgumentException();
		

		Document doc;
		try {
			doc = Jsoup.connect(albumUrl).timeout(Constants.JSOUP_TIMEOUT).get();
			// System.out.print(doc);
			Elements items = doc.select("#albumThumbMode li");
			for (Element item : items) {
				try {
					Element link = item.select("span a").get(0);
					Element thumbPic = item.select("span a img").get(0);

					// 图片描述
					String picDescription = link.attr("title");
					// 大图地址
					String bigPicUrl = getBigPicUrl(link.attr("href"));
					// 缩略图地址
					String thumbPicUrl = thumbPic.attr("src");
					
					System.out.println();
					System.out.println(picDescription);
					System.out.println(thumbPicUrl);
					System.out.println(bigPicUrl);
					
					PicInfo picInfo=new PicInfo();
					picInfo.mThumbUrl=thumbPicUrl;
					picInfo.mBigUrl=bigPicUrl;
					picInfo.mDescription=picDescription;
					albumInfo.mPics.add(picInfo);
				} catch (Exception e) {
					// TODO:log
					e.printStackTrace();
				}
			}
			
			//如果该相册有多页继续爬取下一页
			Elements elements = doc.select(".chn[title=下一页]");
			// check element count,==1.if >1 ?
			if (elements.size() > 0) {
				Element nextPageElement = elements.get(0);
				String nextPageUrl = nextPageElement.attr("href");

				// System.out.println();
				// System.out.println();

				getAlbumPics(nextPageUrl,albumInfo);
			}
		} catch (IOException e) {
			// TODO:log
			e.printStackTrace();
		}
	}

	private String getBigPicUrl(String url) throws IOException {
		String bigPicUrl = null;
		
		Document doc;
		doc = Jsoup.connect(url).timeout(Constants.JSOUP_TIMEOUT).get();
		Elements elements=doc.select("#photoLink img");
		if(elements.size()>0)
			bigPicUrl=elements.get(0).attr("src");
		return bigPicUrl;
	}

}
