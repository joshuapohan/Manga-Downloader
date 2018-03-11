package com.webcrawl;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

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

    
    /** 
    *WebCrawler constructor
    *Initialize the writer for the log
    *@param logPath - the path to the log text file
    **/
    WebCrawler(String logPath){
        super();
        try{
        	FileWriter logWriter = new FileWriter(logPath,true);
        	BufferedWriter buffWriter = new BufferedWriter(logWriter);
        	PrintWriter logPrinter = new PrintWriter(buffWriter);        	
            this.LogPrinter = logPrinter;
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }

    /**
    *@param pattern - the regex pattern of the url to get the manga name
    **/
    public void SetMangaNamePattern(String pattern){
        this.MangaNameRegexPattern = pattern;
    }

    /**
    *@param pattern - the regex pattern of the url to get the manga name
    **/
    public void SetMangaChapterPattern(String pattern){
        this.ChapterRegexPattern = pattern;
    }

        /**
    *@param pattern - the regex pattern of the url to get the manga name
    **/
    public void SetMangaPagePattern(String pattern){
        this.PageRegexPattern = pattern;
    }

    private String getDirFromURL(String url){
        String finalDir = "";
        String[] urlParts = url.split("://");
        if(urlParts.length > 1){
            String filePath = urlParts[1];
            String[] dirs = filePath.split("/");
            int i;
            for(i=0;i< dirs.length - 1;i++){
                finalDir += dirs[i];
                finalDir += '/';
            }
            finalDir.replaceAll(".", "");
            finalDir.replaceAll(" ", "");
            System.out.println(finalDir);
            this.PrintToLog(finalDir);
            return finalDir;
        }
        return finalDir; 
    }

    private String getFileNameFromURL(String url){
        String fileName = "";
        String[] urlParts = url.split("://");
        if(urlParts.length > 1){
            String filePath = urlParts[1];
            String[] dirs = filePath.split("/");
            int length = dirs.length;
            fileName += dirs[length-1];
            fileName.replaceAll("\\s+", "");
            String[] absoluteURL = fileName.split("\\?");
            String absFileName = absoluteURL[0];
            System.out.println(absFileName);
            this.PrintToLog(absFileName);
            return absFileName;
        }
        return fileName; 
    }

    public boolean isURLForMangaChapter(String URL,String MangaName){
        System.out.println("URL : " + URL + this.isChapterLink(URL));
        return URL.contains(MangaName) && this.isChapterLink(URL);
    }

    public boolean isURLPartOfNextMangaPage(String URL, String Chapter, String Page){
        String nextPage;

        if(Page != ""){
             nextPage = Integer.toString(Integer.parseInt(Page) + 1);
        }
        else{
            nextPage = "";
        }
        String patternToFind = Chapter + '/' + nextPage;
        return URL.contains(patternToFind); 
    }

    public boolean isImageOfMangaPage(Element theImage){
        Element parentElement = theImage.parent();
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
    *@param ImgURL - the absolute url to the image to be saved
    *@param MangaName - Manga name as folder name
    *@param Chapter - Chapter number as folder name
    **/
    public boolean isImageSaveSuccessful(String PageURL, String ImgURL){
        try{
            //String fileName = this.getFileNameFromURL(ImgURL);
            //String fileDir = this.getDirFromURL(ImgURL);

            String mangaName = this.getNameFromURL(PageURL);
            String mangaChapter = this.getChapterFromURL(PageURL);
            String fileName = this.getPageFromURL(PageURL) + ".jpg";

            //Use manga name and chapter as download directory
            String fileDir = mangaName + '/' + mangaChapter;
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

    public boolean isSearchForChaptersSuccessful(String URL){
        try{
            Connection connection = Jsoup.connect(URL).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            this.HTMLDoc = htmlDoc;
            if(connection.response().statusCode() == 200){
                if(connection.response().contentType().contains("text/html")){
                    /**
                    *Begin searching for next page of the current chapter
                    **/
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    for(Element link : linksOnPage){
                        String absURL = link.absUrl("href");
                        if(this.isURLForMangaChapter(absURL,this.getNameFromURL(URL))){
                            System.out.println("Chapter" + absURL);
                            this.isCrawlForImageSuccessful(absURL);
                        }
                    }
                    return true;
                }
                else{
                    this.LogPrinter.println("**Failure** Retreived something other than HTML");
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
    *@param urlThe url to search for all the images
    *@return whether or not image elements are succesfully searched for
    **/
    public boolean isCrawlForImageSuccessful(String url){
        try{
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            this.HTMLDoc = htmlDoc;
            if(connection.response().statusCode() == 200){

                System.out.println("\n**Visiting** Received web page at " + url);
                if(connection.response().contentType().contains("text/html")){

                    /**
                    *Begin searching for page image 
                    **/                  
                    Elements imagesOnPage = htmlDoc.select("img");
                    
                    System.out.println("Found (" + imagesOnPage.size() + ") images");
                    this.LogPrinter.println(this.getNameFromURL(url) + " chapter" +  this.getChapterFromURL(url));
                    
                    for(Element image : imagesOnPage){
                        if(this.isImageOfMangaPage(image)){
                            String imgURL = image.absUrl("src");
                            this.LogPrinter.println(imgURL);
                            if(this.isImageSaveSuccessful(url, imgURL)){
                                System.out.println("\n**Succesfully Saved Image**" + imgURL);
                            }
                        }
                    }

                    /**
                    *Begin searching for next page of the current chapter
                    **/
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    for(Element link : linksOnPage){
                        String absURL = link.absUrl("href");
                        if(this.isURLPartOfNextMangaPage(absURL,this.getChapterFromURL(url),this.getPageFromURL(url))){
                            System.out.println("Crawling to" + absURL);
                            if(this.isCrawlForImageSuccessful(absURL)){
                            }
                            break;
                        }
                    }

                    return true;
                }
                else{
                    this.LogPrinter.println("**Failure** Retreived something other than HTML");
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
    *@param urlThe url to search for all the links to other pages
    *@return whether or not the crawl is successful
    **/
    public boolean isCrawlSuccessful(String url){
    	try{
    	    Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
    	    Document htmlDoc = connection.get();
    	    this.HTMLDoc = htmlDoc;
    	    if(connection.response().statusCode() == 200){

                System.out.println("\n**Visiting** Received web page at " + url);
                if(connection.response().contentType().contains("text/html")){
                	
                    Elements linksOnPage = htmlDoc.select("a[href]");

                    System.out.println("Found (" + linksOnPage.size() + ") links");
                    
                    for(Element link : linksOnPage){
                    	this.WebLinks.add(link.absUrl("href"));
                    	this.LogPrinter.println(link.absUrl("href"));
                        System.out.println(link.absUrl("href"));
                    }
                    return true;
                }
                else{
                	this.LogPrinter.println("**Failure** Retreived something other than HTML");
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
    *@param searchWord - the word or string to be searched
    *@return whether or not the word was found
    **/
    public boolean isWordFound(String searchWord){
    	if(this.HTMLDoc == null){
    		System.out.println("Error! call crawl() before performing search");
    		return false;
    	}
    	System.out.println("Searching for the word");
    	String bodyText = this.HTMLDoc.body().text();
    	return bodyText.toLowerCase().contains(searchWord.toLowerCase());
    }

    /**
    *@url - the url to get the chapter from
    *@return the chapter
    **/
    public String getPageFromURL(String URL){
        //String mangaPattern = "c\\d+\\/(\\d+)";
        String mangaPattern = this.PageRegexPattern;         
        Pattern page = Pattern.compile(mangaPattern);
        Matcher m = page.matcher(URL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    public boolean isChapterLink(String URL){
        String mangaPattern = "\\/c\\d+";          
        Pattern chapter = Pattern.compile(mangaPattern);
        Matcher m = chapter.matcher(URL);
        if(m.find()){
            return true;
        }
        return false;
    }

    /**
    *@url - the url to get the chapter from
    *@return the chapter
    **/
    public String getChapterFromURL(String URL){
        //String mangaPattern = "\\/c(\\d+)";
        String mangaPattern = this.ChapterRegexPattern;            
        Pattern chapter = Pattern.compile(mangaPattern);
        Matcher m = chapter.matcher(URL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    /**
    *@url - the url to get the name from
    *@return the book name
    **/
    public String getNameFromURL(String URL){
        //String mangaPattern = "\\bmanga\\/(\\w+)";
        String mangaPattern = this.MangaNameRegexPattern;            
        Pattern name = Pattern.compile(mangaPattern);
        Matcher m = name.matcher(URL);
        if(m.find() && m.groupCount() > 0){
            return m.group(1);
        }
        return "";
    }

    /**
    *to do : add method to parse url
    * get manga name , chapter
    * add method to fill list of links of current page
    * get link with image in page
    * check if link match manga name and chapter pattern, if found matching link 
    * call method to fill list with links and get link with image in page
    *( recursive ? pass list of links with images and 
    *
    *
    *
    *Method parameter : String URL , List of links of image
    *Recursize
    *methodA(String URL , List<String> ImageLinks, List<String> visitedLinks){
    * 
    *       append image linkj
    *       foreach links in page
    *          if link not in list of visited url and count < max page
    *          if link matches pattern 
    *          call methodA()
    *
    *
    *}
    *@param
    *
    *
    *@return
    *
    **/
    /**public boolean isMatchingMangaPattern(String URL){
         String mangaPattern = "\bmanga\/(.*$)";         
         Pattern chapter = Pattern.compile(mangaPattern);
         Matcher  = chapter.matcher("Test");
         if (m.find( )) {
         }
    }**/

    /**
    *getter for links
    **/
    public List<String> getLinks(){
        return this.WebLinks;
    }

    /**
    *This method prints the input to the opened log file
    *@param Log : The log entry to be written to the file
    **/
    public void PrintToLog(String Log){
        if(this.LogPrinter != null){
            this.LogPrinter.println(Log);
        }
    }

    /**
    *close writer
    **/
    public void closeLog(){
        this.LogPrinter.close();
    }
}