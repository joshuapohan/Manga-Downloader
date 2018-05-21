import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClientA{

	JTextArea inbox;
	JTextField outbox;
	PrintWriter writer;
	BufferedReader reader;
	String user;
	Socket sock;

	public static void main(String[] args){
		ChatClientA client = new ChatClientA(args[0]);
		client.go();
	}

	ChatClientA(String name){
		this.user = name;
	}

	public void go(){
		JFrame frame = new JFrame("Chat Client");
		JPanel mainPanel = new JPanel();
		inbox = new JTextArea(15,20);
		inbox.setLineWrap(true);
		inbox.setWrapStyleWord(true);
		inbox.setEditable(false);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendActionListener());
		outbox = new JTextField(20);
		JScrollPane qScroller = new JScrollPane(inbox);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(qScroller);
		mainPanel.add(outbox);
		mainPanel.add(sendButton);
		setUpNetworking();

		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		frame.getContentPane().add(BorderLayout.CENTER,mainPanel);
		frame.setSize(400,500);
		frame.setVisible(true);
	}

	public void setUpNetworking(){
		try{
			sock = new Socket("127.0.0.1",5000);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			writer.println(this.user);
			writer.flush();
			writer.println("exit");
			writer.flush();
			System.out.println("Network Established");
		}
		catch(IOException ex){
			ex.printStackTrace();
		}

	}

	public class SendActionListener implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			try{
				writer.println(outbox.getText());
				writer.flush();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			outbox.setText("");
			outbox.requestFocus();
		}
	}

	public class IncomingReader implements Runnable{
		public void run(){
			String message;
			try{
				while((message = reader.readLine()) != null){
					inbox.append(message + "\n");
				}
			}
			catch(Exception ex){ex.printStackTrace();}
		}
	}
		
}

