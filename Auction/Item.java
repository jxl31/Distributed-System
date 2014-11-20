/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description: 
 */

import java.util.ArrayList;

/*
 * This class will be instantiated as an object that will be shared through each client that is within the server
 * This will make the synchronization of the items shared with the clients more efficient
 */
public class Item {
	private String itemName;
	private float reservePrice;
	private float currentBid;
	private boolean status;
	private int bidderID;
	private boolean ready;
	private ArrayList<Integer> bidderList;
	
	public Item(String itemName, float reservePrice, boolean status){
		this.setItemName(itemName);
		this.setReservePrice(reservePrice);
		this.setStatus(status);
		this.setCurrentBid(reservePrice);
		this.bidderID = 0;
		bidderList = new ArrayList<Integer>();
		ready = false;
	}
	
	public synchronized void setCurrentBidder(float bid, int bidderID){
		this.setBidderID(bidderID);
		this.setCurrentBid(bid);
		bidderList.add(bidderID);
		notifyAll();
	}

	public String getItemName() {
		return itemName;
	}

	private void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public float getReservePrice() {
		return reservePrice;
	}

	private void setReservePrice(float reservePrice) {
		this.reservePrice = reservePrice;
	}

	public float getCurrentBid() {
		return currentBid;
	}

	public void setCurrentBid(float currentBid) {
		this.currentBid = currentBid;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getBidderID() {
		return bidderID;
	}

	public void setBidderID(int bidderID) {
		this.bidderID = bidderID;
	}
	
	@Override
	public String toString(){
		return "Item Name: " + getItemName() + "\n"+
				"Item Reserve Price: " + getReservePrice() + "\n" +
				"Item Current bid: " + getCurrentBid();
	}
}
