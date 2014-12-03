package com.shuai.hehe.crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.shuai.hehe.crawler.data.BlogInfo;
import com.shuai.hehe.crawler.data.Constants;
import com.shuai.hehe.crawler.data.DataManager;
import com.shuai.hehe.crawler.data.FromType;
import com.shuai.hehe.crawler.data.VideoInfo;

/**
 * 热门日志爬虫
 */
public class BlogCrawler {

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

    private static int MAX_VIDEO_COUNT = 1000;

    public BlogCrawler(String startUrl) {
        mStartUrl = startUrl;
    }

    public void start() {
        String url = mStartUrl;
        while (url != null && url.length() > 0) {
            url = getBlogs(url);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取该页面包含的日志
     * 
     * @param url
     *            从该页面爬取日志
     */
    private String getBlogs(String url) {
        url = url.trim();

        System.out.println(String.format("正在爬取日志列表，url:%s", url));

        ++mPageCount;

        Document doc;
        try {
            doc = Jsoup.connect(url).userAgent(Constants.USER_AGENT)
            //                  .cookie("p", "33fb45a605c8ce65e63595467994811f8")
            //                    .cookie("t", "35aad5365c66978703f1448e65f90aa28")
                    .timeout(Constants.JSOUP_TIMEOUT).get();
            // System.out.print(doc);
            Elements items = doc.select(".share-hot-list li");

            //System.out.println("pagecount:" + items.size());
            for (Element element : items) {
                try {
                    BlogInfo info = new BlogInfo();
                    Element link = element.select("h3 a[href]").get(0);
                    String title = link.ownText();
                    if (DataManager.getInstance().isFeedExist(title))
                        continue;

                    String summary = element.select("div.content p").get(0).ownText();
                    String href = link.attr("href");
                    String content = getBlogContent(href, info);

                    System.out.println();
                    System.out.println("count:" + ++mVideoCount);
                    System.out.println("日志：" + title);
                    System.out.println(href);
                    System.out.println(summary);
                    System.out.println(content);

                    info.setTitle(title);
                    info.setWebUrl(href);
                    info.setSummary(summary);
                    info.setHtmlContent(content);
                    info.setfromType(FromType.FROM_RENREN);

                    CrawlerMananger.getInstance().addBlog(info);

                    if (mVideoCount >= MAX_VIDEO_COUNT)
                        return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            Elements elements = doc.select("div.page_wrapper a[title=下一页]");
            if (elements.size() > 0) {
                Element nextPageElement = elements.get(0);
                String nextPageUrl = nextPageElement.attr("href");

                return nextPageUrl;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从url指定的页面抓取日志内容
     * 
     * @param url
     * @return
     * @throws IOException
     */
    private String getBlogContent(String url, BlogInfo info) throws IOException {
        Document doc = Jsoup.connect(url).userAgent(Constants.USER_AGENT)
                .cookie("p", Constants.RENREN_P_KEY)
                .cookie("t", Constants.RENREN_T_KEY)
                .cookie("id", Constants.RENREN_ID)
                .timeout(Constants.JSOUP_TIMEOUT)
                .get();

        Element element = doc.select("div.content-body").get(0);
        String content = element.outerHtml();
        info.setHtmlContent(content);

        return content;
    }

}
