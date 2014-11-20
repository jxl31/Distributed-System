/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description: 
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class AuctionServer implements Runnable {
	private static int PORT = 1235;
	private static int MAX = 50; //maximum of clients that can enter the server
	private final int TIMELIMIT = 60;
	private ServerSocket server = null;
	private Thread thread = null;
	private Vector<AuctionServerThread> clients = new Vector<AuctionServerThread>(); //serializable array
	private int count = 0; //keeps count of the amount of clients currently connected to this instance
	private Item item;
	private BufferedReader reader;
	private Timer timer;
	private int time = 60;
	private Clock clock;
	private boolean timeFlag = false; //flag for starting the clock when a client is accepted
	private boolean timeRun = true; //flag for while loop in clock to stop it from running if no more items
	
	private static String LOW_BID = "Your bid is lower than the current bid.\n Please bid higher";
	private final String NEW_WINNER = "new winner";
	private final String NOT_SOLD = "not sold";
	private final String NEW_BIDDER = "new bidder";
	private final String NEW_ITEM = "new item";
	private final String NO_ITEMS = "no items";
	//message(message,item)

	public AuctionServer(){
		try {
			System.out.println("Binding to port " + PORT + ", please wait  ...");
			server = new ServerSocket(PORT);
			System.out.println("Server started: " + server.getInetAddress());
			init();
			
		} catch (IOException e) {
			System.out.println("Can not bind to port " + PORT + ": " + e.getMessage());
		}
	}
	
	/**
	 * Gets everything started
	 * file handling
	 * clock
	 * initiate thread with start()
	 */
	public void init(){
		setItemFileReader();
		setNextItem();
		start();
	}
	
	public void stopTimer(){
		timer.cancel();
	}
	
	public void newTimer(){
		time = 60;
		timer = new Timer();
		clock = new Clock();
		timer.schedule(clock, 2 * 1000);
	}

	/**
	 * This run is run from start() where the parent thread is created.
	 */
	public void run() {
		while (thread != null){
			try{
				System.out.println("Waiting for a client ...");
				addThread(server.accept());
				
				int pause = (int)(Math.random()*3000);
				Thread.sleep(pause);

			} catch(IOException ioe){
				System.out.println("Server accept error: " + ioe);
				stop();
			} catch (InterruptedException e){
				System.out.println(e);
			}
		}
	}
	
	/**
	 * 
	 * @param socket
	 */
	private void addThread(Socket socket){
		if(count < MAX){
			System.out.println("Client accepted: " + socket);
			clients.add(new AuctionServerThread(this, socket, item));
			clients.get(count).open();
			clients.get(count).start();
			//set two second delay after client is accepted
			count++;
			if(count == 1 && !timeFlag){
				timeFlag = true;
				timeRun = true;
				newTimer();
			}
			clients.get(count-1).send(new Message(item.toString() + "\nTime Remaining: " + time));
		}else{
			System.out.println("Client refused entry: server capacity reached maximum : " + MAX);
		}
	}

	/**
	 * Finds the client using its ID(port) who initiated an action in the pool(array) of clients and returns
	 * its index inside the pool.
	 * @param ID
	 * @return
	 */
	public int findClient(int ID){
		for (int i = 0; i < count; i++){
			if (clients.get(i).getID() == ID){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Sends messages to all clients.
	 * This method is called from the client's server thread and can also be called from the main server's thread
	 */
	public synchronized void quit(int ID, String message){
		
		System.out.println(ID + ", bid " + item.getCurrentBid());
		if(message.equals("quit")){
			clients.get(findClient(ID)).send(new Message("quit"));
			remove(ID);
		}
	}
	
	/**
	 * Sends the message to all clients
	 * @param message
	 */
	public synchronized void broadcast(Message message){
		if(message.getMessageType() == NEW_BIDDER){
			clock.resetClock();
			System.out.println("Resetting Clock for new bidder");
		}
		
		for(int i = 0; i < count; i++){
			clients.get(i).send(message);
		}
		notifyAll();
	}
	/**
	 * Sends time remaining to all client
	 * @param time
	 */
	public synchronized void broadcastTime(int time){
		for(int i = 0; i < count; i++){
			clients.get(i).send(new Message("Time left till next item: " + time));
		}
		notifyAll();
	}

	/**
	 * called when the client sent "QUIT" or "quit" to server
	 * @param ID ID of the client to be removed
	 */
	public synchronized void remove(int ID){
		int pos = findClient(ID);
		if (pos >= 0){
			AuctionServerThread toTerminate = clients.get(pos);
			clients.remove(pos);
			System.out.println("Removing client thread " + ID + " at " + pos);
			count--;
			
			try{
				//closes the connection between server and client
				toTerminate.close();
			}
			catch(IOException ioe)
			{
				System.out.println("Error closing thread: " + ioe);
			}
			toTerminate = null;
			System.out.println("Client " + pos + " removed");
			if(count == 0){
				timeFlag = false;
				timeRun = false;
			}
			notifyAll();
		}
	}
	
	/**
	 * Used when there is no more item to be auction. It will remove 
	 * all clients currently connected to the server.
	 */
	public synchronized void removeAll(){
		for(int i = count-1; i > 0; i--){
			remove(clients.get(i).getID());
		}
	}
	
	/**
	 * closing thread
	 */
	public void stop(){
		thread = null;
	}
	/**
	 * method for starting the run()
	 */
	public void start(){
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	/**
	 * Reading from a file containing items that will be sold
	 */
	public void setItemFileReader(){
		String filename = "AuctionItems.txt";
		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File is not found :" + filename);
		}
	}
	
	/**
	 * Sets new item object for auction from file
	 */
	public void setNextItem(){
		try {
			item = null;
			String line = "";
			if((line = reader.readLine()) != null){}
			else{
				timeRun = false;
				broadcast(new Message(NO_ITEMS, item));
				removeAll();
				stop();
				System.exit(1);
			}
			String[] itemDetails = line.split(",");
			String itemName = itemDetails[0];
			float price = Float.parseFloat(itemDetails[1]);
			boolean status = Boolean.parseBoolean(itemDetails[2]);
			item = new Item(itemName, price, status);
		} catch (IOException e) {
			System.out.println("Cannot read from file. " + e.getMessage());
		}
		
	}
	
	/**
	 * This is an inner class that will be used for the timer.
	 * Method contains the run method which is a thread and inside is where the counter
	 * also contains resetClock() which will change the time to go back to 60sec
	 * @author John
	 *
	 */
	class Clock extends TimerTask{
		@Override
		public void run() {
			System.out.println("Clock started");
			while(timeRun){
				try {
					Thread.sleep(1*1000);
					time--;
					System.out.println(time);
					//broadcastTime(time--);
					if(time <= 1){
						if(item.getBidderID() != 0){
							//broadcastCtrl(item.getBidderID(), true,false,false); //winner
							broadcast(new Message(NEW_WINNER, item));
							setNextItem();
							//broadcastCtrl(0, false, true,true); //new item
							broadcast(new Message(NEW_ITEM, item));
							clock.resetClock();
						}
						else{
							broadcast(new Message(NOT_SOLD, item));
							//broadcastCtrl(0, false, false,false); //no winner
							setNextItem();
							broadcast(new Message(NEW_ITEM, item));
							//broadcastCtrl(0, false, true,false); //new item
							clock.resetClock();
						}
					}
					
					if(time == TIMELIMIT/2){
						broadcast(new Message("\n\n--------WARNING: Time Remaining " + time +"--------\n"));
					}else if(time  == 10){
						broadcast(new Message("\n\n--------WARNING: Time Remaining " + time +"--------\n"));
					}
				} catch (InterruptedException e) {
					System.out.println("Clock error: " + e.getMessage());
				}
			}
			System.out.println("Clock terminated");
		}
		
		public int getTime(){
			return time;
		}
		
		public void resetClock(){
			time = 60;
		}
		
	}
	
	/**
	 * sends a message to the client that bid lower than the current bid
	 * @param ID
	 */
	public void bidInvalid(int ID){
		clients.get(findClient(ID)).send(new Message(LOW_BID));
	}
	
	public static void main(String[] args) {
		new AuctionServer(); //starts the server
	}

}
