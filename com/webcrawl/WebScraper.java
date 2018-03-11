package com.webcrawl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class WebScraper{

	private static final int MAX_PAGES_TO_SEARCH = 20;
	private Set<String> PagesVisited = new HashSet<String>();
	private List<String> PagesToVisit =new LinkedList<String>();
    private String RootURL;
    private String StringToSearch; 
    private String LogPath;

    public static void main(String[] args){
    	run();
    }
    
    public static void run()
    {
        WebScraper scraper = new WebScraper();
        //scraper.RunSearch("http://arstechnica.com/", "computer");
        //if(scraper.isUserInputValid()){
        //    scraper.RunSearch();
        //}
        //begin debug 
        scraper.runImageSearch();


    }

    public void runImageSearch(){
        Scanner consoleInput = new Scanner(System.in);
        System.out.println("Enter the path to the log file");
        this.SetLogPath(consoleInput.nextLine());
        System.out.println("Enter the base URL");
        this.SetRootURL(consoleInput.nextLine());
        WebCrawler crawler = new WebCrawler(this.LogPath);

        crawler.SetMangaNamePattern("\\bmanga\\/(\\w+)");
        crawler.SetMangaChapterPattern("\\/c(\\d+)");
        crawler.SetMangaPagePattern("c\\d+\\/(\\d+)");

       if(crawler.isSearchForChaptersSuccessful(this.RootURL)){
        //if(crawler.isCrawlForImageSuccessful(this.RootURL)){
            crawler.closeLog();
        }
    }

    /**
    *Setter for log path
    *@param url : String of log path
    **/
    public void SetLogPath(String path){
        this.LogPath = path;
    }

    /**
    *Setter for root url to crawl
    *@param url : String of the url to crawl
    **/
    public void SetRootURL(String url){
        this.RootURL = url;
    }

    /**
    *Setter for word to search
    *@param word : String of  the word to search
    **/
    public void SetStringToSeach(String word){
        this.StringToSearch = word;
    }

    /**
    *This method asks the user for input
    *
    *
    *@return boolean whether or not user correctly input the parameter
    *
    **/
    private boolean isUserInputValid(){
        Scanner consoleInput = new Scanner(System.in);
        System.out.println("Enter the path to the log file");
        this.SetLogPath(consoleInput.nextLine());
        System.out.println("Enter the base URL");
        this.SetRootURL(consoleInput.nextLine());
        System.out.println("Enter the word to searh");
        this.SetStringToSeach(consoleInput.nextLine());
        return true;
    }


    /**
    *Main function to launch the scraper, it creates a WebCrawler 
    *that makes the HTTP request and parse the response
    *
    *@param url
    *       - the starting url
    *@param searchWord
    *       - the word or string to search
    */
    public void RunSearch(){
    	while(this.PagesVisited.size() < MAX_PAGES_TO_SEARCH){
    		String currentUrl;
    		WebCrawler crawler = new WebCrawler(this.LogPath);
            System.out.println("new crawler instance");
    		if(this.PagesToVisit.isEmpty()){
    			currentUrl = this.RootURL;
    			this.PagesVisited.add(currentUrl);
    		}
    		else{
    			currentUrl = this.nextUrl();
    		}
    		if(crawler.isCrawlSuccessful(currentUrl)){
                System.out.println("crawl successfull");
    		    boolean success = crawler.isWordFound(this.StringToSearch);
    		    if(success){
    			    System.out.println(String.format("**Success** Word %s found at %s",this.StringToSearch, currentUrl));
                    crawler.PrintToLog(String.format("**Success** Word %s found at %s",this.StringToSearch, currentUrl));
    		        //break;
    		    }
    		    this.PagesToVisit.addAll(crawler.getLinks());
                crawler.closeLog();
    	    }
    	}
    	System.out.println("\n**Done** Visited " + this.PagesVisited.size() + " web page(s)");
    }

    /**
    *Returns the next url to visit
    *
    *
    *@return
    */
    private String nextUrl(){
    	String nextUrl;
    	do{
    		nextUrl = this.PagesToVisit.remove(0);
    	} while(this.PagesVisited.contains(nextUrl));
    	this.PagesVisited.add(nextUrl);
    	return nextUrl;
    } 
}