cd C:\Users\ap com\Documents\JAVA\webcrawler 4
javac -cp jsoup-1.6.0.jar com\webcrawl\*.java
jar cfm ChapterDownload.jar MANIFEST.MF com\webcrawl\*.class
java -jar ChapterDownload.jar
pause