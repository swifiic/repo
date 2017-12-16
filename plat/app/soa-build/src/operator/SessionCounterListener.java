package operator;

import java.util.HashMap;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSession;
 
public class SessionCounterListener implements HttpSessionListener {
 
  private static int totalActiveSessions;
  private static final HashMap<String, HttpSession> sessions = new HashMap<String, HttpSession>();
 
  @Override
  public void sessionCreated(HttpSessionEvent event) {
	totalActiveSessions++;
		HttpSession session = event.getSession(); 
		sessions.put(session.getId(), session);
  }
 
  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
	totalActiveSessions--;
	sessions.remove(event.getSession().getId());
  }	
  
public static HttpSession getSession(String sessionId){
	return sessions.get(sessionId);
}

public static void removeSession(String sessionId){
	sessions.remove(sessionId);
}
}