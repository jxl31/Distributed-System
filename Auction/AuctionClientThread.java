/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description:
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;


public class AuctionClientThread extends Thread{
	private Socket socket   = null;
    private AuctionClient auctionClient   = null;
    private ObjectInputStream  streamIn = null;
    private String clientName = null;
    
    public AuctionClientThread(AuctionClient auctionClient, Socket socket){
    	this.auctionClient = auctionClient;
    	this.socket = socket;
    	this.clientName = clientName;
    	open();
    	start();
    }
    
    /**
     * Establish the input stream from the server
     */
    public void open(){
    	try{
    		streamIn = new ObjectInputStream(socket.getInputStream());
    	}
    	catch(IOException e){
    		System.out.println("Error getting input stream: " +socket);
    		auctionClient.stop();
    	}
    }
    
    /**
     * if there is an error. close the input stream from the client
     */
    public void close(){
    	try{
    		if(streamIn != null){
    			streamIn.close();
    		}
    	}
    	catch(IOException e){
    		System.out.println("Error closing input stream: " +socket);
    	}
    }
    
    public void run(){
    	while(true && auctionClient != null){
    		try{
        		auctionClient.handle((Message)streamIn.readObject());
    		} catch(IOException e){
    			auctionClient = null;
    			System.out.println("Listening Error: " + e.getMessage());
    			System.exit(1);
    		} catch(ClassNotFoundException e){
    			auctionClient = null;
    			System.out.println("Catching Message Error: " + e.getMessage());
    			System.exit(1);
    		}
    	}
    }
}
