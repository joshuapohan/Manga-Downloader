package com.webcrawl;

import java.sql.*;

public class MangaDatabase{

	Connection DBConnection;
	Statement GlobalStmt = null;

	/**
	*Initialize connection to the database
	*/
	MangaDatabase(){
		try{
			Class.forName("org.sqlite.JDBC");
			this.DBConnection = DriverManager.getConnection("jdbc:sqlite:manga.db");
        	System.out.println("Connected to database successfully");
        	this.GlobalStmt = this.DBConnection.createStatement();

        } catch ( Exception e ) {
        	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        	System.exit(0);
      	}
	}

	public boolean GenerateTable(){
		if(this.DBConnection != null && this.GlobalStmt != null){
			try{
				String sql = "CREATE TABLE MANGA" +
								" (ID	    INT    PRIMARY KEY     NOT NULL," +
								" NAME      TEXT   NOT NULL," +
								" CHAPTER   INT," +
								" COMPLETED CHAR(5)," +
								" URL       TEXT   NOT NULL)";
        		this.GlobalStmt.executeUpdate(sql);
        		DBConnection.commit();
        	} catch ( Exception e ) {
        		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        		System.exit(0);
      		}
        }
        return true;
	}

	public boolean InsertMangaRow(String ID, String MangaName, String LatestChapter, String IsCompleted, String MangaURL){
		if(this.DBConnection != null && this.GlobalStmt != null){
			try{
				String sql  = "INSERT INTO MANGA (ID, NAME, CHAPTER, COMPLETED, URL) " +
								"VALUES (" + ID + ",'" + MangaName + "','" + LatestChapter + "','" + IsCompleted +  "','" + MangaURL + "');";
				this.GlobalStmt.executeUpdate(sql);
				DBConnection.commit();
			} catch ( Exception e ) {
        		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        		System.exit(0);
      		}
		}
		return true;
	}

	public void CloseConnection(){
		if(this.DBConnection != null){
			try{
				this.DBConnection.close();
			} catch ( Exception e ) {
        		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        		System.exit(0);
      		}
		}
	}
}