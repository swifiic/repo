package in.swifiic.app.msngr.andi;

import java.util.ArrayList;
import java.util.List;

import in.swifiic.app.msngr.andi.R;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.ui.UserChooserActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
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
		final DatabaseHelper db = new DatabaseHelper(this);
		Cursor c = db.getFirstMessageForAllUsers();
		db.closeDB();
		final ChatSummaryCursorAdapter adapter = new ChatSummaryCursorAdapter(this, c);
		Log.d(TAG, "Setting cursor!");
		c.moveToFirst();
		adapter.changeCursor(c);
		final ListView chatList = (ListView) findViewById(R.id.list);
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
		chatList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		chatList.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			int count = 0;
			List<String> selectedItems = new ArrayList<String>();
			
			@Override
		    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		        // Here you can do something when items are selected/de-selected,
		        // such as update the title in the CAB
				View listItem = (View) chatList.getChildAt(position);
				TextView userNameView = (TextView) listItem.findViewById(R.id.firstLine);
				if (checked) {					
					selectedItems.add(userNameView.getText().toString());
					++count;
				} else {
					selectedItems.remove(userNameView.getText().toString());
					--count;
				}
				mode.invalidate();
		    }

		    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
		        switch (item.getItemId()) {
		            case R.id.delete_chat:
		                DatabaseHelper dbh = new DatabaseHelper(chatList.getContext());
		                dbh.deleteMessagesForUserIds(selectedItems);
		                dbh.closeDB();
		                adapter.changeCursor(db.getFirstMessageForAllUsers());
		                mode.finish(); // Action picked, so close the CAB
		                return true;
		            default:
		                return false;
		        }
		    }

		    @Override
		    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		        // Inflate the menu for the CAB
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.context_chat_summary, menu);
		        return true;
		    }

		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
		    }

		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        mode.setTitle(count + " items selected");
		        return true;
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
