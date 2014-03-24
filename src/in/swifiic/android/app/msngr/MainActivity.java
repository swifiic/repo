package in.swifiic.android.app.msngr;

import java.util.Date;
import java.util.List;

import in.swifiic.android.app.lib.AppEndpointContext;
import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.ui.SwifiicActivity;
import in.swifiic.android.app.lib.ui.UserChooserActivity;
import in.swifiic.android.app.lib.xml.Action;
import in.swifiic.android.app.lib.xml.Notification;
import in.swifiic.android.app.msngr.DatabaseHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SwifiicActivity {
    
    private static final int SELECT_USER = 1;
    
    private static final String TAG ="MainActivity";
	
    private TextView mTextUserList = null;
    private EditText mTextMsgToSend = null;
    private TextView mTextFromOthers = null;
    
    private AppEndpointContext aeCtx = new AppEndpointContext("Messenger", "0.1", "1");
    
    DatabaseHelper db;
    
    /**
     * Mandatory implementation to receive swifiic notifications
     */
    public MainActivity() {
    	super();
    	// This is a must for all applications - hook to get notification from GenericService
    	mDataReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("notification")) {
                	String message= intent.getStringExtra("notification");
                	
                    Log.d("MAIN", "Handling incoming message: " + message);
                    Notification notif = Helper.parseNotification(message);
                	// Checking for opName of Notification
                    if(notif.getNotificationName() == "DeliverMessage") {
                    	Msg msg = new Msg(notif);
                    	db = new DatabaseHelper(getApplicationContext());
                    	db.addMessage(msg);
                        mTextFromOthers.append(Html.fromHtml(msg.getPrintableMessage()));
                        final ScrollView mScrollView = (ScrollView)findViewById(R.id.scrollView1);        
                        mScrollView.post(new Runnable() {			
                			@Override
                			public void run() {
                				mScrollView.fullScroll(View.FOCUS_DOWN);
                			}
                		});
                    }
                } else {
                    Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
                }
            }
        };
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.itemSelectUser) {
			Intent selectNeighbor = new Intent(this, UserChooserActivity.class);
			startActivityForResult(selectNeighbor, SELECT_USER);
			return true;
		} else if (itemId == R.id.settings) {
			Intent selectedSettings = new Intent(this, SettingsActivity.class);
			startActivity(selectedSettings);
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
    }

    /**
     * Called when activity exits from "startActivityForResult"
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SELECT_USER == requestCode) {
            if ((data != null)){
            	String userName = "";
            	if (data.hasExtra("userName")) {
            		userName = data.getStringExtra("userName");
            	}
            	mTextUserList.setText(userName);
            	Log.d("ActivityResult", "Got user: " + userName);
            	mTextFromOthers = (TextView)findViewById(R.id.textMessages);
            	mTextFromOthers.setText("");
            	db = new DatabaseHelper(getApplicationContext());
            	List<Msg> msgs = db.getMessagesForUser(userName);
            	if(msgs != null) {
	            	int size = msgs.size();
	            	for(int i=0; i<size; ++i) {
	            		mTextFromOthers.append(Html.fromHtml(msgs.get(i).getPrintableMessage()));
	            	}
            	}
            	final ScrollView mScrollView = (ScrollView)findViewById(R.id.scrollView1);        
                mScrollView.post(new Runnable() {			
        			@Override
        			public void run() {
        				mScrollView.fullScroll(View.FOCUS_DOWN);
        			}
        		});
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /** 
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_msngr);
        
        mTextMsgToSend = (EditText)findViewById(R.id.msgTextToSend);
        mTextUserList = (TextView)findViewById(R.id.usrListToSend);
        mTextFromOthers = (TextView)findViewById(R.id.textMessages);        
        
        TextView viewHubAddress = (TextView) findViewById(R.id.hubAddress);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        viewHubAddress.setText("Configured Hub: " + prefs.getString("hub_address", "Hub broadcast not yet received"));
        
        // Assign an action to the send button
        Button b = (Button)findViewById(R.id.buttonSendMsg);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(mTextUserList.getText().equals("Select User")) {
            		Context context = getApplicationContext();
            		Toast toast = Toast.makeText(context, "Select a user first!", Toast.LENGTH_SHORT);
            		toast.setGravity(Gravity.TOP, 0, 100);
            		toast.show();
            	}
            	else if (mTextMsgToSend.getText().toString().equals("")){
            		// Do nothing
            	}
            	else {
                    Action act = new Action("SendMessage", aeCtx);
                    act.addArgument("message", mTextMsgToSend.getText().toString());
                    act.addArgument("toUser", mTextUserList.getText().toString());
                    Date date = new Date();
                    act.addArgument("sentAt", "" + date.getTime());
                    
                    
                    // Loading hub address from preferences
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    String hubAddress = sharedPref.getString("hub_address", "");
                    
                    Helper.sendAction(act, hubAddress + "/messenger", v.getContext());
                    Notification ntf = Helper.parseNotification(Helper.serializeAction(act));
                    Msg msg = new Msg(ntf);
                    db = new DatabaseHelper(v.getContext());
                    db.addMessage(msg);
                    EditText msgInput = (EditText) findViewById(R.id.msgTextToSend);
                    msgInput.setText("");
                    mTextFromOthers = (TextView) findViewById(R.id.textMessages);
                    mTextFromOthers.append(Html.fromHtml(msg.getPrintableMessage()));
                    final ScrollView mScrollView = (ScrollView)findViewById(R.id.scrollView1);        
                    mScrollView.post(new Runnable() {			
            			@Override
            			public void run() {
            				mScrollView.fullScroll(View.FOCUS_DOWN);
            			}
            		});
            	}
            }
        });
    }

    public void onResume() {
        super.onResume();
        TextView viewHubAddress = (TextView) findViewById(R.id.hubAddress);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        viewHubAddress.setText("Configured Hub: " + prefs.getString("hub_address", "Hub broadcast not yet received"));   
    }
}
