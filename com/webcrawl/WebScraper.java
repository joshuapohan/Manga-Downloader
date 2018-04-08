package com.webcrawl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class WebScraper{

	private static final int MAX_PAGES_TO_SEARCH = 20;
	private Set<String> PagesVisited = new HashSet<String>();
	private List<String> PagesToVisit = new LinkedList<String>();
    private String RootURL;
    private String StringToSearch; 
    private String LogPath;
    private String DLPath;
    private WebCrawler crawler;
    private MangaDatabase MangaDB;

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
        scraper.RunMangaDownloader();
        //scraper.StoreMangaReferenceInDB();
    }

    private void SetUpCrawler(){
        this.crawler.InitializeLogFile("testy.txt");
        this.crawler.SetMangaNamePattern("\\bmanga\\/(\\w+)");
        this.crawler.SetMangaChapterPattern("\\/c(\\d+)");
        this.crawler.SetMangaPagePattern("c\\d+\\/(\\d+)");
        this.crawler.SetMangaChapterLinkPattern("c\\d+\\/\\d$");
    }

    public void StoreMangaReferenceInDB(){
        WebCrawler crawler = new WebCrawler();
        this.crawler = crawler;
        this.SetUpCrawler();
        MangaDatabase mangaDB = new MangaDatabase();
        this.MangaDB = mangaDB;
        mangaDB.GenerateTable();
        this.StoreMangaInfoFromURL("https://mangapark.net/manga/volcanic-age-tomato");
    }

    public void StoreMangaInfoFromURL(String TheURL){
        if(this.crawler != null){
            String mangaName = this.crawler.getNameFromURL(TheURL);
            String mangaChapter = "1";
            String IsCompleted = "False";

            if(this.MangaDB != null){
                if(this.MangaDB.InsertMangaRow(mangaName, mangaName, mangaChapter, IsCompleted, TheURL)){

                }
            }
        }
    }

    public void RunMangaDownloader(){
        Scanner consoleInput = new Scanner(System.in);

        System.out.println("Enter the download directory :");
        this.SetDownloadPath(consoleInput.nextLine());

        System.out.println("Enter the path to the log file");
        this.SetLogPath(consoleInput.nextLine());

        System.out.println("Enter the base URL");
        this.SetRootURL(consoleInput.nextLine());

        WebCrawler crawler = new WebCrawler();

        //Initialize the log file for the web crawler
        crawler.InitializeLogFile(this.LogPath);

        crawler.SetDownloadDirectory(this.DLPath);

        crawler.SetMangaNamePattern("\\bmanga\\/(\\w+)");
        crawler.SetMangaChapterPattern("\\/c(\\d+)");
        crawler.SetMangaPagePattern("c\\d+\\/(\\d+)");
        crawler.SetMangaChapterLinkPattern("c\\d+\\/\\d$");
        crawler.SetUserInputPattern("\\d+");
        
        if(crawler.isSearchForChaptersSuccessful(this.RootURL)){
            System.out.println("List of chapters :");
            crawler.PrintChapterHashMap();
            System.out.println("Enter chapter to download (Format : ddd)");
            crawler.DownloadSelectedChapters(consoleInput.nextLine());
            //if(crawler.DownloadAllChaptersInPage(this.RootURL)){
            crawler.closeLog();
            //}
        }

    }

    /**
    *Setter for log path
    *@param url String of log path
    */
    public void SetLogPath(String path){
        this.LogPath = path;
    }

    /**
    *Setter for root url to crawl
    *@param url String of the url to crawl
    */
    public void SetRootURL(String url){
        this.RootURL = url;
    }

    /**
    *Setter for download path
    *@param path path to file download
    */
    public void SetDownloadPath(String path){
        this.DLPath = path;
    }

    /**
    *Setter for word to search
    *@param word : String of  the word to search
    */
    public void SetStringToSeach(String word){
        this.StringToSearch = word;
    }

    /**
    *This method asks the user for input
    *@return boolean whether or not user correctly input the parameter
    */
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
    *@param url the starting url
    *@param searchWord the word or string to search
    */
    public void RunSearch(){
    	while(this.PagesVisited.size() < MAX_PAGES_TO_SEARCH){
    		String currentUrl;
    		WebCrawler crawler = new WebCrawler();
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
    *@return the next url to visit
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