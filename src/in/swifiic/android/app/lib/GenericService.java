package in.swifiic.android.app.lib;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import in.swifiic.android.app.lib.Constants;
import in.swifiic.android.app.lib.xml.Action;
import in.swifiic.android.app.lib.xml.Notification;
import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.Bundle.ProcFlags;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.GroupEndpoint;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionConnection;
import de.tubs.ibr.dtn.api.SessionDestroyedException;
import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.api.TransferMode;


/**
 * right now a messenger specific service
 * will handle XML strings generically in future
 * @author abhishek
 *
 */
public class GenericService extends IntentService {
    
    // This TAG is used to identify this class (e.g. for debugging)
    private static final String TAG = "GenericService";
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    
    // The communication with the DTN service is done using the DTNClient
    private DTNClient mClient = null;
    
    // Hold the last message as result
    private StringBuffer mLastMessage = new StringBuffer("");
    


    public GenericService() {
        super(TAG);
    }
    
    
    public String getLastMessage() {
    	String toReturn = mLastMessage.toString();
    	mLastMessage = new StringBuffer("");;
        return toReturn;
    }
    
    private final IBinder mBinder = new LocalBinder();
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */ 
    public class LocalBinder extends Binder {
        public GenericService getService() {
            return GenericService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

  
    
    private void sendToHub(String message, String hubAddress) {
        // create a new bundle
        Bundle b = new Bundle();

        SingletonEndpoint destination = new SingletonEndpoint(hubAddress);
        
        // set the destination of the bundle
        b.setDestination(destination);
        
        // limit the lifetime of the bundle to 60 seconds
        b.setLifetime(Constants.LONG_LIFETIME); 
        
        // set status report requests for bundle reception
        b.set(ProcFlags.REQUEST_REPORT_OF_BUNDLE_RECEPTION, true);
        
        // set destination for status reports
        b.setReportto(SingletonEndpoint.ME);
        
        
        try {
            // get the DTN session
            Session s = mClient.getSession();
            
            // send the bundle
            BundleID ret = s.send(b, message.getBytes());
            
            if (ret == null)
            {
                Log.e(TAG, "could not send the message");
            }
            else
            {
                Log.d(TAG, "Bundle sent, BundleID: " + ret.toString() + "Bundle Source:" + mClient.getDTNService().getEndpoint());
            }
        } catch (SessionDestroyedException e) {
            Log.e(TAG, "could not send the message", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "could not send the message", e);
        } catch (RemoteException e) {
        	Log.e(TAG,"Sent but with remote exception in logs");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(null==intent) {
        	Log.e(TAG, "Received Null Intent - ignoring");
        	return;
        }
        String action = intent.getAction();
        if(null==action) {
        	Log.e(TAG, "Received Null action - ignoring");
        	return;
        }
        Log.d(TAG, "Handling Intent:"+ action);
        if (de.tubs.ibr.dtn.Intent.RECEIVE.equals(action))
        {
            // Received bundles from the DTN service here
            try {
                // We loop here until no more bundles are available
                // (queryNext() returns false)
                while (mClient.getSession().queryNext()){
                	Log.e(TAG, "Attempting to processs a BUNDLE !!!!!!");
                }
            } catch (SessionDestroyedException e) {
                Log.e(TAG, "Can not query for bundle", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "Can not query for bundle", e);
            }
        }
        else if (Constants.MARK_DELIVERED_INTENT.equals(action))
        {
            // retrieve the bundle ID of the intent
            BundleID bundleid = intent.getParcelableExtra("bundleid");
            
            try {
                // mark the bundle ID as delivered
                mClient.getSession().delivered(bundleid);
            } catch (Exception e) {
                Log.e(TAG, "Can not mark bundle as delivered.", e);
            }
        }
        else if (Constants.REPORT_DELIVERED_INTENT.equals(action))
        {
            // retrieve the source of the status report
            SingletonEndpoint source = intent.getParcelableExtra("source");
            
            // retrieve the bundle ID of the intent
            BundleID bundleid = intent.getParcelableExtra("bundleid");
            
            // TODO MSG_DELIVERED_APPHUB should be sent to activity : BUT we need to map bundleId to Action Request Number
            
            Log.d(TAG, "Status report received for " + bundleid.toString() + " from " + source.toString());
        }
        else if (Constants.SEND_MSG_INTENT.equals(action))
        {
            // retrieve the Action object message
        	String msg = intent.getStringExtra("action");
        	String hubAddress = intent.getStringExtra("hub_address");
        	
            try {
                Serializer serializer = new Persister();
                @SuppressWarnings("unused")  // this is checking that XML is fine
				Action req = serializer.read(Action.class, msg);
                sendToHub(msg, hubAddress);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG,"Parse failed during send message for String:"+msg);
			}
        }
    }
    
    SessionConnection mSession = new SessionConnection() {

        @Override
        public void onSessionConnected(Session session) {
            Log.d(TAG, "Session connected");
            session.setDataHandler(mDataHandler);
        }

        @Override
        public void onSessionDisconnected() {
            Log.d(TAG, "Session disconnected");
        }
        
    };
    
    
    GroupEndpoint GE_TEST=null;
    @Override
    public void  onCreate() {
        super.onCreate();
        
        // create a new DTN client
        if(null == mClient) {
        	mClient = new DTNClient(mSession);
        	String appName = this.getApplication().getPackageName();
        	Log.d("GenericService", "Registered with: " + appName);
            Registration registration = new Registration(appName);
        	GE_TEST=new GroupEndpoint("dtn://"+appName + "/mc");
            registration.add(GE_TEST);
            
            try {
                // initialize the connection to the DTN service
                mClient.initialize(this, registration);
                Log.d(TAG, "Connection to DTN service established. GE=" + GE_TEST + " endpoint=" +appName);
            } catch (ServiceNotAvailableException e) {
                // The DTN service has not been found
                Log.e(TAG, "DTN service unavailable. Is IBR-DTN installed?", e);
            } catch (SecurityException e) {
                // The service has not been found
                Log.e(TAG, "The app has no permission to access the DTN service. It is important to install the DTN service first and then the app.", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        // terminate the DTN service
        mClient.terminate();
        mClient = null;
        
        super.onDestroy();
    }

    /**
     * This data handler is used to process incoming bundles
     */
    private DataHandler mDataHandler = new DataHandler() {

        private Bundle mBundle = null;

        @Override
        public void startBundle(Bundle bundle) {
            // store the bundle header locally
            mBundle = bundle;
        }

        @Override
        public void endBundle() {
            // complete bundle received
            BundleID received = new BundleID(mBundle);
            
            
            // mark the bundle as delivered
            Intent i = new Intent(GenericService.this, GenericService.class);
            i.setAction(Constants.MARK_DELIVERED_INTENT);
            i.putExtra("bundleid", received);
            startService(i);
            
            // notify other components of the updated value
            Intent updatedIntent = new Intent(Constants.NEWMSG_RECEIVED);
            updatedIntent.putExtra("bundleid", received);
            String msg = getLastMessage();

            try {
                Serializer serializer = new Persister();
                @SuppressWarnings("unused")  // this is checking that XML is fine
				Notification notif = serializer.read(Notification.class,msg);
                Log.d(TAG, "Got a message: " + msg.toString());
	            updatedIntent.putExtra("notification", msg);
	            if(null != GE_TEST && mBundle.getDestination().equals(GE_TEST)) {
	            	updatedIntent.putExtra("multicast", "true");
	            	Log.d(TAG, "Got via Multicast");
	            }
	            sendBroadcast(updatedIntent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG,"Parse failed for String"+msg);
			}
            // free the bundle header
            mBundle = null;
        }
        
        @Override
        public TransferMode startBlock(Block block) {
            // we are only interested in payload blocks (type = 1)
            if (block.type == 1) {
                // return SIMPLE mode to received the payload as "payload()" calls
                return TransferMode.SIMPLE;
            } else {
                // return NULL to discard the payload of this block
                return TransferMode.NULL;
            }
        }

        @Override
        public void endBlock() {
            // nothing to do here.
        }

        @Override
        public ParcelFileDescriptor fd() {
            // This method is used to hand-over a file descriptor to the
            // DTN service. We do not need the method here and always return
            // null.
            return null;
        }

        @Override
        public void payload(byte[] data) {
            // payload is received here
        	mLastMessage.append(new String(data));
            Log.d(TAG, "payload received: " + data);
        }

        @Override
        public void progress(long offset, long length) {
            // if payload is written to a file descriptor, the progress
            // will be announced here
            Log.d(TAG, offset + " of " + length + " bytes received");
        }
    };
}
