/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description: 
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
//import java.util.*;

public class AuctionServerThread extends Thread{
	private AuctionServer server = null;
	private Socket socket   = null;
	private int ID = -1;
	/*
	 * This streamIn and streamOut are specifically for the client that instantiate this thread within the server
	 * this streams will be used to read in data that the client sent to the server and
	 * the ways in which the server writes to the client.
	 */
	private DataInputStream  inputFromClient  =  null; 
	private ObjectOutputStream outputToClient = null;
	@SuppressWarnings("unused")
	private Thread thread;
	private Item item;
	private final String NEW_BIDDER = "new bidder";

	/**
	 * Constructor for AuctionServerThread
	 * @param server instance of AuctionServer
	 * @param socket instance of the listening socket
	 */
	public AuctionServerThread(AuctionServer server, Socket socket, Item item){
		super(); //new thread()
		this.server = server;
		this.socket = socket;
		this.ID = socket.getPort();
		this.item = item;
	}

	public void run(){
		System.out.println("Server Thread " + ID + " running.");
		thread = new Thread(this);
		while (true){
			try{
				String message = inputFromClient.readUTF();
				float bid;
				if(message.equals("quit")){
					server.quit(ID,message);
				}else if((bid = Float.parseFloat(message)) <= item.getCurrentBid()){
					server.bidInvalid(ID);
				}else{
					item.setCurrentBidder(bid, ID); //set new bidder
					server.broadcast(new Message(NEW_BIDDER, this.item));
				}

				int pause = (int)(Math.random()*3000);
				Thread.sleep(pause);
			}
			catch (InterruptedException e)
			{
				System.out.println(e);
			}
			catch(IOException ioe){
				server.remove(ID);
				thread = null;
			}
		}
	}

	/**
	 * Returns the id of this client instance
	 * @return ID
	 */
	public int getID(){
		return this.ID;
	}

	/**
	 * Sending message from server to client
	 * @param message
	 */
	public void send(Message message){
		try{
			outputToClient.writeObject(message);
			outputToClient.flush();
		}catch(IOException e){
			System.out.println("Error in writing to client: "+ ID + ". Error is " + e.getMessage());
			server.remove(ID);
			thread = null;
		}
	}

	/**
	 * establishes the streams for reading and writing that the server will use
	 */
	public void open(){
		try {
			inputFromClient = new DataInputStream(new
					BufferedInputStream(socket.getInputStream()));
			outputToClient = new ObjectOutputStream(socket.getOutputStream());
//					new DataOutputStream(new
//					BufferedOutputStream(socket.getOutputStream()));
		} catch (IOException e) {
			System.out.println("Error in establishing streams : " + e.getMessage());
		}
	}

	/**
	 * Close all streams and socket of this client
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		if (socket != null)
			socket.close();
		
		if (inputFromClient != null)
			inputFromClient.close();

		if (outputToClient != null)
			outputToClient.close();
	}
}
