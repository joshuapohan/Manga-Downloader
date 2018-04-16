package com.webcrawl;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler{

	//Use Mozilla USER_AGENT
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    //List to hold all the links in the document
    private List<String> WebLinks = new LinkedList<String>();
    //HTML Document from jsoup connection get
    private Document HTMLDoc;
    //The writer for the lgo
    public PrintWriter LogPrinter;

    private String MangaNameRegexPattern;
    private String ChapterRegexPattern;
    private String PageRegexPattern;
    private String ChapterLinkRegexPattern;
    private String UserInputChapterRegexPattern;

    private String CurrentMangaName;

    private String DownloadDirectory;

    private HashMap<String,String> ChapterLinksHashMap = new HashMap<>();
    
    /** 
    *WebCrawler constructor
    */
    WebCrawler(){
        super();
    }

    /**
    *Initialize the writer for the log
    *@param LogPath the path to write the log file
    */
    public void InitializeLogFile(String LogPath){
        try{
            FileWriter logWriter = new FileWriter(LogPath,true);
            BufferedWriter buffWriter = new BufferedWriter(logWriter);
            PrintWriter logPrinter = new PrintWriter(buffWriter);           
            this.LogPrinter = logPrinter;
            System.out.println("Log initialized");
        }
        catch(IOException ex){
            System.out.println(ex);
        }        
    }

    public void SetUserInputPattern(String pattern){
        this.UserInputChapterRegexPattern = pattern;
    }

    /**
    *@param pattern the regex pattern of the url to get the manga name
    */
    public void SetMangaNamePattern(String pattern){
        this.MangaNameRegexPattern = pattern;
    }

    /**
    *@param pattern the regex pattern of the url to get the manga name
    */
    public void SetMangaChapterPattern(String pattern){
        this.ChapterRegexPattern = pattern;
    }

    /**
    *@param pattern the regex pattern of the url to check whether or not link is part of manga
    */
    public void SetMangaChapterLinkPattern(String pattern){
        this.ChapterLinkRegexPattern = pattern;
    }

    /**
    *@param pattern the regex pattern of the url to get the manga name
    */
    public void SetMangaPagePattern(String pattern){
        this.PageRegexPattern = pattern;
    }

    /**
    *@param path path to download directory
    */
    public void SetDownloadDirectory(String path){
        this.DownloadDirectory = path;
    }


    /**
    *check whether or not the passed url contains manga name and is link to chapter
    *@param TheURL the link to test
    *@param MangaName the manga name
    *@return whether or not the url is the link to the mange chapter
    */
    public boolean isURLForMangaChapter(String TheURL){
        //Debug Code
        System.out.println("URL : " + TheURL + this.isChapterLink(TheURL));
        //Debug Code
        return TheURL.contains(this.CurrentMangaName) && this.isChapterLink(TheURL);
    }

    /**
    *Check whether or not passed url is part of manga next page url
    *@param BaseURL the link to test
    *@return whether or not the url is the link to the next page in the chapter
    */
    public boolean isURLPartOfNextMangaPage(String BaseURL, String URLToTest){
        String nextPage;

        String currentChapter = this.getChapterFromURL(BaseURL);
        String currentPage = this.getPageFromURL(BaseURL);

        if(currentPage != ""){
             nextPage = Integer.toString(Integer.parseInt(currentPage) + 1);
        }
        else{
            nextPage = "";
        }
        String patternToFind = currentChapter + '/' + nextPage;
        return URLToTest.contains(patternToFind); 
    }

    /**
    *check whether or not current element image is of the manga page
    *@param TheImage the image element 
    *@return whether or not the image element is of the manga page
    */
    public boolean isImageOfMangaPage(Element TheImage){
        Element parentElement = TheImage.parent();
        if(parentElement != null){
            if(parentElement.className() != null){
                System.out.println(parentElement.className());
                if(parentElement.className().equals("img-link")){
                    System.out.println("True page");
                    return true;
                }
            }
        }
        return false;
    }

    /**
    *@param PageURL the current page URL to get the current chapter and page
    *@param ImgURL the absolute url to the image to be saved
    */
    public boolean isImageSaveSuccessful(String PageURL, String ImgURL){
        try{
            String mangaName = this.CurrentMangaName;
            String mangaChapter = this.getChapterFromURL(PageURL);
            String fileName = this.getPageFromURL(PageURL) + ".jpg";

            //Use manga name and chapter as download directory
            String fileDir = this.DownloadDirectory + '/' + mangaName + '/' + mangaChapter;
            File fileDirectory = new File(fileDir);

            System.out.println(fileDir);
            System.out.println("Created "  + fileDirectory.mkdirs());
            
            //Response resultImageResponse = Jsoup.connect(ImgURL).cookies(cookies).ignoreContentType(true).execute();
            Response resultImageResponse = Jsoup.connect(ImgURL).userAgent(USER_AGENT).execute();
            
            System.out.println("Saving file to " + fileDir + '/' + fileName);
            
            //Write image bytecode to image file
            FileOutputStream out = (new FileOutputStream(new java.io.File(fileDir + '/' + fileName)));
            out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
            out.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        return true;
    }

    /**
    *@param PageURL the root url to get links to all the chapters
    *@return whether or not chapter dpwnload was successful
    */
    public boolean DownloadAllChaptersInPage(String PageURL){
        this.isSearchForChaptersSuccessful(PageURL);
        this.DownloadFromStoredURLList();
        this.PrintChapterHashMap();
        return true;
    }


    /**
    *@param TheURL the base url to search all the links to the chapters
    *@return whether or not chapter crawl was successful
    */
    public boolean isSearchForChaptersSuccessful(String TheURL){
        try{
            Connection connection = Jsoup.connect(TheURL).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            this.HTMLDoc = htmlDoc;
            if(connection.response().statusCode() == 200){
                if(connection.response().contentType().contains("text/html")){

                    this.CurrentMangaName = this.getNameFromURL(TheURL);

                    //Begin searching for next page of the current chapter
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    for(Element link : linksOnPage){
                        String absURL = link.absUrl("href");
                        if(this.isURLForMangaChapter(absURL)){
                            String curChapter = this.getChapterFromURL(absURL);
                            if(curChapter.length() >  0){
                                if(this.ChapterLinksHashMap != null){
                                    if(!this.ChapterLinksHashMap.containsKey(curChapter)){
                                        this.ChapterLinksHashMap.put(curChapter,absURL);
                                    }
                                }
                                else{
                                    this.PrintToLog("**Error** Chapter links not initialized");
                                    return false;
                                }
                            }
                            else{
                                this.PrintToLog("**Error** Failed to get chapter from URL " + absURL);
                            }
                        }
                    }
                    return true;
                }
                else{
                    this.PrintToLog("**Error** Retrieved something other than HTML");
                    return false;
                }
            }
        }
        catch(IOException ex){
            return false;
        }
        return false;
    }

    /**
    *Parse user input to get the selected chapter to download
    *@return successfully parsed the input and downloaded the chapters
    */
    public boolean DownloadSelectedChapters(String ChToDownload){
        int chapterCount = 0;
        String patternString = this.UserInputChapterRegexPattern;            
        Pattern chPattern = Pattern.compile(patternString);
        Matcher m = chPattern.matcher(ChToDownload);
        if(!ChToDownload.equals("all")){
            while(m.find()){
                if(this.DownloadSelectedChapter(m.group())){
                    chapterCount++;
                }
            }
            System.out.println("Downloaded " + chapterCount + " chapters");
        }
        else{
            return this.DownloadFromStoredURLList();
        }

        return false;
    }

    /**
    *Download chapter
    *@param TheChapter string of chapter number
    *@return if chapter is found and succesffully downloaded
    */
    public boolean DownloadSelectedChapter(String TheChapter){
        if(this.ChapterLinksHashMap != null && this.ChapterLinksHashMap.size() > 0){
            if(this.ChapterLinksHashMap.containsKey(TheChapter)){
                this.PrintToLog("Chapter Found");
                String ChapterURL = this.ChapterLinksHashMap.get(TheChapter);
                this.isCrawlForImageSuccessful(ChapterURL);
                return true;
            }
        }
        return false;
    }


    /**
    *Print hash map key and value to command line
    *@return if hash map succesfully printed
    */
    public boolean PrintChapterHashMap(){
        if(this.ChapterLinksHashMap != null && this.ChapterLinksHashMap.size() > 0){
            if(this.LogPrinter != null){
                Iterator<Map.Entry<String,String>> hashIt  = this.ChapterLinksHashMap.entrySet().iterator();
                while(hashIt.hasNext()){
                    Map.Entry<String,String> curChEntry = hashIt.next();
                    System.out.println(curChEntry.getKey() + "," + curChEntry.getValue());
                    this.LogPrinter.println(curChEntry.getKey() + "," + curChEntry.getValue());
                }
                return true;
            }
        }
        return false;
    }
    /**
    *iterates through link previously retrieved
    *@return whether or not the retrieval was successful
    */
    private boolean DownloadFromStoredURLList(){
        if(this.ChapterLinksHashMap != null && this.ChapterLinksHashMap.size() > 0){
            if(this.LogPrinter != null){
                Iterator<Map.Entry<String,String>> hashIt  = this.ChapterLinksHashMap.entrySet().iterator();
                while(hashIt.hasNext()){
                    Map.Entry<String,String> curChEntry = hashIt.next();
                    this.isCrawlForImageSuccessful(curChEntry.getValue());
                    this.PrintToLog("Downloaded " + curChEntry.getValue());
                }
                return true;
            }
        }
        return false;
    }

    /**
    *@param TheURL The url to search for all the images
    *@return whether or not image elements are succesfully searched for
    */
    public boolean isCrawlForImageSuccessful(String TheURL){
        try{
            Connection connection = Jsoup.connect(TheURL).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            this.HTMLDoc = htmlDoc;
            if(connection.response().statusCode() == 200){

                System.out.println("\n**Visiting** Received web page at " + TheURL);
                if(connection.response().contentType().contains("text/html")){

                    //List all images element in current page            
                    Elements imagesOnPage = htmlDoc.select("img");
                    
                    //Debug code   
                    System.out.println("Found (" + imagesOnPage.size() + ") images");
                    //Debug code

                    for(Element image : imagesOnPage){
                        if(this.isImageOfMangaPage(image)){
                            String imgURL = image.absUrl("src");
                            //this.PrintToLog(imgURL);
                            if(this.isImageSaveSuccessful(TheURL, imgURL)){
                                //Debug code
                                //System.out.println("\n**Succesfully Saved Image**" + imgURL);
                                //Debug code
                            }
                        }
                    }
                    /**
                    *Begin searching for next page of the currnt chapter
                    */
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    for(Element link : linksOnPage){
                        String chapterLink = link.absUrl("href");
                        if(this.isURLPartOfNextMangaPage(TheURL, chapterLink)){
                            
                            //Debug code
                            System.out.println("Crawling to" + chapterLink);
                            //Debug code
                            
                            if(this.isCrawlForImageSuccessful(chapterLink)){
                            }
                            break;
                        }
                    }
                    return true;
                }
                else{
                    this.PrintToLog("**Failure** Retreived something other than HTML");
                    return false;
                }
            }
        }
        catch(IOException ex){
            return false;
        }
        return false;
    }

    /**
    *@param TheURL The url to search for all the links to other pages
    *@return whether or not the crawl is successful
    */
    public boolean isCrawlSuccessful(String TheURL){
    	try{
    	    Connection connection = Jsoup.connect(TheURL).userAgent(USER_AGENT);
    	    Document htmlDoc = connection.get();
    	    this.HTMLDoc = htmlDoc;
    	    if(connection.response().statusCode() == 200){

                System.out.println("\n**Visiting** Received web page at " + TheURL);
                if(connection.response().contentType().contains("text/html")){
                	
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    System.out.println("Found (" + linksOnPage.size() + ") links");
                    
                    for(Element link : linksOnPage){
                    	this.WebLinks.add(link.absUrl("href"));
                    	this.PrintToLog(link.absUrl("href"));
                        System.out.println(link.absUrl("href"));
                    }
                    return true;
                }
                else{
                	this.PrintToLog("**Failure** Retreived something other than HTML");
                    return false;
                }
            }
    	}
    	catch(IOException ex){
    		return false;
    	}
        return false;
    }

    /**
    *Performs the search on the html document
    *@param SearchWord the word or string to be searched
    *@return whether or not the word was found
    */
    public boolean isWordFound(String SearchWord){
    	if(this.HTMLDoc == null){
    		System.out.println("Error! call crawl() before performing search");
    		return false;
    	}
    	System.out.println("Searching for the word");
    	String bodyText = this.HTMLDoc.body().text();
    	return bodyText.toLowerCase().contains(SearchWord.toLowerCase());
    }

    /**
    *@param TheURL the url to get the chapter from
    *@return the chapter
    */
    public String getPageFromURL(String TheURL){
        String pagePattern = this.PageRegexPattern;         
        Pattern page = Pattern.compile(pagePattern);
        Matcher m = page.matcher(TheURL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    /**
    *@param TheURL the URL to check whether or not it is link to the chapter
    *@return whether or not the passed link is link to chapter
    */
    public boolean isChapterLink(String TheURL){
        System.out.println(this.ChapterLinkRegexPattern);          
        String chapterPattern = this.ChapterLinkRegexPattern;
        Pattern chapter = Pattern.compile(chapterPattern);
        Matcher m = chapter.matcher(TheURL);
        if(m.find()){
            return true;
        }
        return false;
    }

    /**
    *@param TheURL the url to get the chapter from
    *@return the chapter
    */
    public String getChapterFromURL(String TheURL){
        String mangaPattern = this.ChapterRegexPattern;            
        Pattern chapter = Pattern.compile(mangaPattern);
        Matcher m = chapter.matcher(TheURL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    /**
    *@param TheURL the url to get the name from
    *@return the book name
    */
    public String getNameFromURL(String TheURL){
        String mangaPattern = this.MangaNameRegexPattern;            
        Pattern name = Pattern.compile(mangaPattern);
        Matcher m = name.matcher(TheURL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    /**
    *getter for links
    */
    public List<String> getLinks(){
        return this.WebLinks;
    }

    /**
    *{rints the input to the opened log file
    *@param TheMessage The log entry to be written to the file
    */
    public void PrintToLog(String TheMessage){
        if(this.LogPrinter != null){
            this.LogPrinter.println(TheMessage);
        }
        else{
            System.out.println("Error printing log, log file not initialized. The message : " + TheMessage);
        }
    }

    /**
    *close writer
    */
    public void closeLog(){
        this.LogPrinter.close();
    }

    /**
    *reset web crawler parameters
    */
    public void ResetParameters(){
        this.closeLog();
        this.MangaNameRegexPattern = "";
        this.ChapterRegexPattern = "";
        this.PageRegexPattern = "";
        this.CurrentMangaName = "";
        this.ChapterLinksHashMap.clear();
    }
}