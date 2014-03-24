package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.xml.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Msg {
	 
    private String msg;
    private String from, to;
    private Date sentAt;
 
    public Msg(Notification notif) {
        this.msg = notif.getArgument("message");
        this.from = notif.getArgument("fromUser");
        this.to = notif.getArgument("toUser");
        this.sentAt = new Date(Long.parseLong(notif.getArgument("sentAt")));
    }
    
    public Msg() {
		// TODO Auto-generated constructor stub
	}

	public String getPrintableMessage() {
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
    	Date date = new Date(Long.parseLong(this.getSentAtTime()));
		return "<small>[" + sdf.format(date) + "]</small><br><strong>" + this.getFrom() + ":</strong> " + this.getMsg() + "<br>";
    }
    
    /*
     * Setters
     */
    public void setMsg(String msg) {
    	this.msg = msg;
    }
    public void setFrom(String from) {
    	this.from = from;
    }
    public void setTo(String to) {
    	this.to = to;
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
    public String getFrom() {
    	return this.from;
    }
    public String getTo() {
    	return this.to;
    }
    public String getSentAtTime() {
    	return "" + this.sentAt.getTime();
    }
}