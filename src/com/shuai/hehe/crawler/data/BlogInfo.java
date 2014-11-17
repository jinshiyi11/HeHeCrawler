package com.shuai.hehe.crawler.data;

/**
 * 日志信息
 */
public class BlogInfo {

    /**
     * 日志标题
     */
    private String mTitle;
    
    /**
     * 日志摘要
     */
    private String mSummary;
    
    /**
     * 该日志的html内容
     */
    private String mHtmlContent;
    
    /**
     * 该日志对应的url页面
     */
    private String mWebUrl;
    
    /**
     * 新鲜事来源
     */
    private int mFromType=FromType.FROM_RENREN;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        this.mSummary = summary;
    }

    public String getHtmlContent() {
        return mHtmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.mHtmlContent = htmlContent;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public void setWebUrl(String webUrl) {
        this.mWebUrl = webUrl;
    }

    public int getFromType() {
        return mFromType;
    }

    public void setfromType(int fromType) {
        this.mFromType = fromType;
    }
    
    

}
