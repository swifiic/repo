package in.swifiic.app.msngr.andi;

import in.swifiic.plat.helper.andi.xml.Notification;

import java.util.Date;


/*
 * Model class for messages
 */
public class Msg {
	 
    private String msg;
    private String user;
    private int isInbound=0;
    private Date sentAt, hubRelayedAt, receivedAt;
    
    public Msg(Notification notif) {
		this.msg = notif.getArgument("message");
		this.user = notif.getArgument("fromUser");
		this.isInbound = 1;
		this.sentAt = new Date(Long.parseLong(notif.getArgument("sentAt")));
        this.hubRelayedAt= new Date(Long.parseLong(notif.getArgument("hubRelayedAt")));
        this.receivedAt= new Date();
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
    public void setHubRelayedAtTime(String time) {
        this.hubRelayedAt = new Date(Long.parseLong(time));
    }
    public void setReceivedAtTime(String time) {
        this.receivedAt = new Date(Long.parseLong(time));
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
    public String getRelayedAtTime() {
        return "" + this.hubRelayedAt.getTime();
    }
    public String getReceivedAtTime() {
        return "" + this.receivedAt.getTime();
    }
}