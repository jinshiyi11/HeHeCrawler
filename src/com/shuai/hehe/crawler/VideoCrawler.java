package com.shuai.hehe.crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.shuai.hehe.crawler.data.Constants;
import com.shuai.hehe.crawler.data.DataManager;
import com.shuai.hehe.crawler.data.VideoInfo;

/**
 * 热门视频爬虫
 */
public class VideoCrawler {
	/**
	 * 爬虫的起始url
	 */
	private String mStartUrl;
	
	/**
	 * 已经爬过的视频数
	 */
	private int mVideoCount;

	/**
	 * 已经爬过的页面数
	 */
	private int mPageCount;
	
	private static int MAX_VIDEO_COUNT=1;
	
	public VideoCrawler(String startUrl) {
		mStartUrl = startUrl;
	}

	public void start() {
		getVideos(mStartUrl);
	}
	
	/**
	 * 获取该页面包含的视频
	 * @param url 从该页面爬取视频
	 */
	private void getVideos(String url) {
		if (url == null)
			new IllegalArgumentException();

		url = url.trim();
		if (url.isEmpty())
			throw new IllegalArgumentException();
		
		System.out.println(String.format("正在爬取视频列表，url:%s",url));

		++mPageCount;

		if (mPageCount > 10)
			return;

		Document doc;
		try {
			doc = Jsoup.connect(url).timeout(Constants.JSOUP_TIMEOUT).get();
			// System.out.print(doc);
			Elements items = doc.select(".share-hot-list li");
			
			System.out.println("pagecount:" + items.size());
			for (Element element : items) {
				try {
					Element link = element.select("h3 a[href]").get(0);
					// TODO:check
					String title = link.ownText();
					String href = link.attr("href");
					String videoUrl = getVideoUrl(href);

					Element div = element.select(".content .video").get(0);
/*
 <div style="background-image: url('http://v3.pfs.56img.com/images/26/24/jasperci56olo56i56.com_sc_13902076340hd.jpg')" class="video">
 <a href="http://share.renren.com/share/406776768/16865052333" target="_blank" onclick="shareLog('list', 'click', 3)">播放</a>
 </div>
 */
					String style = div.attr("style");
					int beginIndex = style.indexOf("('");
					if (beginIndex == -1)
						throw new RuntimeException();

					beginIndex += 2;

					int endIndex = style.indexOf("')");
					if (endIndex == -1)
						throw new RuntimeException();

					String thumbImgUrl = style.substring(beginIndex, endIndex);

					System.out.println("count:" + ++mVideoCount);

					System.out.println();
					System.out.println(title);
					System.out.println(videoUrl);
					System.out.println(thumbImgUrl);

					VideoInfo info = new VideoInfo();
					info.mTitle = title;
					info.mVideoThumbUrl = thumbImgUrl;
					info.mVideoUrl = videoUrl;
					DataManager.getInstance().addHotVideo(info);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			

			Elements elements = doc.select(".chn[title=下一页]");
			// check element count,==1.if >1 ?
			if (elements.size() > 0) {
				Element nextPageElement = elements.get(0);
				String nextPageUrl = nextPageElement.attr("href");

				// System.out.println();
				// System.out.println();

				getVideos(nextPageUrl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 从url指定的页面抓取视频的url
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	private String getVideoUrl(String url) throws IOException {
		String videoUrl = null;

		/*
		 * <div id="sharevideo" style="padding: 0;"> <img class="videoimg" src=
		 * "http://v3.pfs.56img.com/images/26/24/jasperci56olo56i56.com_sc_13902076340hd.jpg"
		 * alt="1.2;http://player.56.com/renrenshare_MTA1MDc5NDcx.swf" /></div>
		 */
		Document doc;
		doc = Jsoup.connect(url).timeout(Constants.JSOUP_TIMEOUT).get();
		// Elements elements = doc.select("embed[src]");
		// videoUrl = elements.get(0).attr("src");

		Elements elements = doc.select(".videoimg");
		videoUrl = elements.get(0).attr("alt");
		videoUrl = videoUrl.substring(videoUrl.indexOf("http:"));
		return videoUrl;
	}

}
