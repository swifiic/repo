package in.swifiic.plat.helper.hub;

public interface SwifiicHandler {
	
	public class Context {
		public String srcUrl;
		
		// TODO add other context for incoming message
	}

	public void handlePayload(String payload, final Context ctx,String srcUrl);
}
