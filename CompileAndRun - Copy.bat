cd C:\Users\ap com\Documents\JAVA\Manga Downloader
javac -cp jsoup-1.6.0.jar com\webcrawl\*.java
pause
jar cfm ChapterDownload.jar MANIFEST.MF com\webcrawl\*.class
pause
java -jar ChapterDownload.jar
pause