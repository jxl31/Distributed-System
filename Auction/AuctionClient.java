/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description: 
 */

import java.net.*;
import java.util.Scanner;
import java.io.*;

//TODO check if input is float and not string (DONE)
//TODO how to display time from server

public class AuctionClient implements Runnable{
	
	private static InetAddress host; //this client
	private static final int PORT = 1235; //server port
	private AuctionClientThread clientThread = null; //thread
	private BufferedReader  inputFromKeyboard = null;
	private DataOutputStream outputToServer = null;
	private Thread thread = null;
	private Socket socket;
	private Scanner scanner;
	private String accountName = "";
	private String message;
	/**
	 * Constructor for AuctinClient Class
	 * creates connection using local IP address and PORT number
	 * calls start()
	 */
	public AuctionClient(String clientName){
		System.out.println("Establishing connection to server. Please wait...");
		try{
			accountName = clientName;
			host = InetAddress.getLocalHost(); //get local IP address
			socket = new Socket(host, PORT); //create socket which listens to port 1234
			System.out.println("Connected to server: " + socket); //inform user that they are connected
			start();
		}
		catch(UnknownHostException e){
			System.out.println("Localhost not found.");
		}
		catch(IOException e){
			System.out.println("Unexpected exception: "+ e.getMessage());
		}
	}
	/**
	 * establishes connection from keyboard and establishes connection for writing to the server
	 * instantiates a new thread when it is first called
	 * @throws IOException
	 */
	
	public void start() throws IOException{
		//set up input stream
		inputFromKeyboard = new BufferedReader(new InputStreamReader(System.in)); 
		//set up output stream
		outputToServer = new DataOutputStream(socket.getOutputStream());
		//if client is empty, instantiate a new thread
		if(thread == null){
			clientThread = new AuctionClientThread(this,socket);
			thread = new Thread(this);
			thread.start(); //initiates this.run()
		}
	}
	
	/**
	 * This is the parent thread's run which is called by start.
	 * This will be the thread to enable a client to write to the server
	 */
	public void run() {
		try{
			Thread.sleep(100); //allows to prepare the information of the current item on offer from server
								//without the sleep. The user is immediately prompted with entering bid.
			
			while(thread != null){
				message = "";
				do{
					enterBid();
					scanner = new Scanner(message);
					if(scanner.hasNextDouble() || message.equals("quit")) break;
					else{
						System.out.println("Please input valid value. Value "+ message + " is not valid.");
					}
				}while(true);
				outputToServer.writeUTF(message);
				outputToServer.flush(); //write to server
				Thread.sleep(100);
			}
		} catch(IOException e){
			System.out.println("Unable to write: " + e.getMessage());
			stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void enterBid(){
		System.out.print(accountName + " enter bid: ");
		try {
			message = inputFromKeyboard.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * called by AuctionClientThread opening input stream throws an exception.
	 * can also be called by this, when writing to server throws an exception
	 */
	public void stop(){
		try{
			if(inputFromKeyboard != null) inputFromKeyboard.close();
			if(outputToServer != null) outputToServer.close();
			if(socket != null) socket.close();
		}
		catch(IOException e){
			System.out.println("Error closing...");
		}
		clientThread.close();
		thread = null;
	}
	
	/**
	 * Outputs the message from the server to the console
	 * @param msg
	 */
	public void handle(Message msg){
		if(msg.equals("QUIT") || msg.equals("quit")){
			System.out.println("Wishing to quit. Press RETURN to exit...");
			stop();
		}else{
			System.out.println(msg.getMessage());
		}
	}
	
	public static void main(String[] args) {
		AuctionClient client = null;
		if(args.length != 1){
			System.out.println("Usage: java AuctionClient Name.");
		}
		new AuctionClient(args[0]);
		//instantiate client --> create socket --> start() --> create input and output stream
	}
}
