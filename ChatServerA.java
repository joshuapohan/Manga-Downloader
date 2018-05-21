/*
 * The BufferedReader will keep on reading the input until it reaches the end (end of file or stream or source etc). In this case, the 'end' is the closing of the socket. So as long as the Socket connection is open, your loop will run, and the BufferedReader will just wait for more input, looping each time a '\n' is reached.
 */



import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerA{

	ArrayList clientOutputStream;

	public class ClientHandler implements Runnable{
		BufferedReader reader;
		Socket sock;
		String name;
		public ClientHandler(Socket clientSocket,String name){
			try{
				sock = clientSocket;
				this.name = name;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
			       reader = new BufferedReader(isReader);
			 }
	 		catch(Exception ex){ex.printStackTrace();}
		}

		public void run(){
			String message;
			try{
				while((message = reader.readLine())!= null){
					System.out.println(message +  "\n");
					message = name + "\t:" + message;
					tellEveryone(message);	
				}
			}
			catch(Exception ex){ex.printStackTrace();}
		}
		
	}

	public static void main(String[] args){
		new ChatServerA().go();
	}

	public void go(){
		clientOutputStream = new ArrayList();
		try{
			ServerSocket serverSock = new ServerSocket(5000);
			while(true){
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
		                System.out.println("test");
				InputStreamReader isReaderName = new InputStreamReader(clientSocket.getInputStream());
			       BufferedReader readerName = new BufferedReader(isReaderName);
			       String title;
			       clientOutputStream.add(writer);
			      while(!(title = readerName.readLine()).equals("exit")){
				      Thread t = new Thread(new ClientHandler(clientSocket,title));
				      t.setName(title);
				      System.out.println(t.getName());
				      t.start();
			      
			      }
			      
			       System.out.println("test");
				/*clientOutputStream.add(writer);
				Thread t = new Thread(new ClientHandler(clientSocket,title));
				t.start();*/
				System.out.println("got a connection \n");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void tellEveryone(String message){
		Iterator it = clientOutputStream.iterator();
		while(it.hasNext()){
			try{
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
}


