package in.swifiic.app.msngr.andi;

import in.swifiic.plat.helper.andi.xml.Notification;

import java.util.Date;


/*
 * Model class for messages
 */
public class Msg {
	 
    private String msg;
    private String user;
    private int isInbound;
    private Date sentAt;
    
    public Msg(Notification notif) {
		this.msg = notif.getArgument("message");
		this.user = notif.getArgument("fromUser");
		this.isInbound = 1;
		this.sentAt = new Date(Long.parseLong(notif.getArgument("sentAt")));
	}
    
	public Msg() {
		// TODO Auto-generated constructor stub
	}

	/*
     * Setters
     */
    public void setMsg(String msg) {
    	this.msg = msg;
    }
    public void setUser(String user) {
    	this.user = user;
    }
    public void setIsInbound(int isInbound) {
    	this.isInbound = isInbound;
    }
    public void setSentAtTime(String time) {
    	this.sentAt = new Date(Long.parseLong(time));
    }
    
    /*
     * Getters
     */ 
    public String getMsg() {
    	return this.msg;
    }
    public String getUser() {
    	return this.user;
    }
    public int getIsInbound() {
    	return this.isInbound;
    }
    public String getSentAtTime() {
    	return "" + this.sentAt.getTime();
    }
}