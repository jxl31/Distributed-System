import java.io.Serializable;

/**
 * Created By: John Xaviery Lucente
 * Institute: Dublin Institute of Technology School of Computing
 * Description: 
 */

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	private String messageType;
	
	private final String NEW_WINNER = "new winner";
	private final String NOT_SOLD = "not sold";
	private final String NEW_BIDDER = "new bidder";
	private final String NEW_ITEM = "new item";
	private final String NO_ITEMS = "no items";
	
	/**
	 * Constructor for messages that does not concern with item
	 * @param m
	 */
	public Message(String m){
		setMessage(m);
	}
	
	/**
	 * Constructor for messages that informs clients about an item
	 * @param m
	 * @param i
	 */
	public Message(String mType, Item i){
		setMessageType(mType);
		if(mType == NEW_BIDDER){
			setMessage(newBidderMessage(i));
		} else if(mType == NOT_SOLD){
			setMessage(notSoldMessage(i));
		} else if(mType == NEW_WINNER){
			setMessage(newWinnerMessage(i));
		} else if(mType == NEW_ITEM){
			setMessage(newItemMessage(i));
		} else if(mType == NO_ITEMS){
			setMessage(noMoreItemsMessage(i));
		}
	}
	
	private String noMoreItemsMessage(Item item){
		return "\nNo more item to be auctioned!.\nThank you all for attending.\nGoodbye!";
	}
	
	private String newItemMessage(Item item){
		return "\nNew item up for auction!\nItem Name: " +item.getItemName()+
				"\nItem Reserve Price: "+item.getReservePrice()+
				"\nItem Current Bid: "+item.getCurrentBid();
	}
	
	private String newWinnerMessage(Item item){
		return "\nWinner winner chicken dinner! We have a winner for item: " + item.getItemName();
	}
	
	private String notSoldMessage(Item item){
		return "\nItem has no bid! Item: " + item.getItemName() + " is not SOLD!";
	}
	
	private String newBidderMessage(Item item){
		return "\n\nAnother client made a new bet on " + item.getItemName() + "!." + "\n" +
				"New price is " + item.getCurrentBid() + ". \n" + 
				"Timer Back to:  60sec\n";
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
}
