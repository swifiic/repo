package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.AppEndpointContext;
import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.ui.SwifiicActivity;
import in.swifiic.android.app.lib.ui.UserChooserActivity;
import in.swifiic.android.app.lib.xml.Action;
import in.swifiic.android.app.lib.xml.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SwifiicActivity {
    
    private static final int SELECT_USER = 1;
    
    private static final String TAG ="MainActivity";
	
    private TextView mTextUserList = null;
    private EditText mTextMsgToSend = null;
    private TextView mTextFromOthers=null;
    
    private AppEndpointContext aeCtx = new AppEndpointContext("Messenger", "0.1", "1");
    
    /**
     * Mandatory implementation to receive swifiic notifications
     */
    public MainActivity() {
    	super();
    	mDataReceiver= new BroadcastReceiver() {  // this is a must for all applications - hook to get notification from GenericService
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("notification")) {
                	String message= intent.getStringExtra("notification");
                	
                    Log.d("MAIN", "TBD XXX -  handle incoming messages" + message);
                    Notification notif = Helper.parseNotification(message);
                	// TODO check opName / Notification name
                    // notif.getNotificationName()

                    String textToUpdate = notif.getArgument("message");
                  
                    mTextFromOthers.append(textToUpdate);
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
        setContentView(R.layout.test_activity_msngr);
        
        mTextMsgToSend = (EditText)findViewById(R.id.msgTextToSend);
        mTextUserList = (TextView)findViewById(R.id.usrListToSend);
        mTextFromOthers = (TextView)findViewById(R.id.textMessages);
        
        TextView viewHubAddress = (TextView) findViewById(R.id.hubAddress);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        viewHubAddress.setText(prefs.getString("hub_address", "Hub broadcast not yet received"));
        
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
            	else {
                    Action act = new Action("SendMessage", aeCtx);
                    act.addArgument("message", mTextMsgToSend.getText().toString());
                    act.addArgument("toUser", mTextUserList.getText().toString());
                    
                    // Loading hub address from preferences
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    String hubAddress = sharedPref.getString("hub_address", "");
                    
                    // TODO - Need to convert user name to userId for uniqueness
                    Helper.sendAction(act, hubAddress + "/messenger", v.getContext());
            	}
            }
        });
    }

    public void onResume() {
        super.onResume();
        TextView viewHubAddress = (TextView) findViewById(R.id.hubAddress);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        viewHubAddress.setText(prefs.getString("hub_address", "Hub broadcast not yet received"));   
    }
}
