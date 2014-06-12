package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.ui.SwifiicActivity;
import in.swifiic.android.app.lib.ui.UserChooserActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ChatSummary extends SwifiicActivity {
	
	private static final int SELECT_USER = 1;
	private String TAG = "ChatSummary";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_summary);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		DatabaseHelper db = new DatabaseHelper(this);
		Cursor c = db.getFirstMessageForAllUsers();
		db.closeDB();
		ChatSummaryCursorAdapter adapter = new ChatSummaryCursorAdapter(this, c);
		Log.d(TAG, "Setting cursor!");
		c.moveToFirst();
		adapter.changeCursor(c);
		ListView chatList = (ListView) findViewById(R.id.list);
		chatList.setAdapter(adapter);
		chatList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView userNameView = (TextView) view.findViewById(R.id.firstLine);
				String userName = userNameView.getText().toString();
				Intent i = new Intent(parent.getContext(), MainActivity.class);
				i.putExtra("userName", userName);
				startActivity(i);
			}
		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_summary, menu);
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
            	Intent i = new Intent(this, MainActivity.class);
            	i.putExtra("userName", userName);
            	startActivity(i);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
