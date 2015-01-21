package in.swifiic.plat.helper.andi;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;


public class UserListLoader extends AsyncTaskLoader<List<User>> {

	@SuppressWarnings("unused")
	// will be needed if we have to pull the list from AppHub
	private GenericService mService = null;

	private Boolean mStarted = false;
	private List<User> mData = null;

	public UserListLoader(Context context, GenericService service) {
		super(context);
		mService = service;
		setUpdateThrottle(250);
	}

	@Override
	public void deliverResult(List<User> data) {
		if (isReset()) {
			mData = null;
			return;
		}

		mData = data;

		if (isStarted()) {
			super.deliverResult(data);
		}
	}

	@Override
	protected void onReset() {
		onStopLoading();

		if (mStarted) {
			// unregister from intent receiver
			// AT Dec 14 getContext().unregisterReceiver(_receiver);
			mStarted = false;
		}
	}

	@Override
	protected void onStartLoading() {
		if (mData != null) {
			this.deliverResult(mData);
		}

		// AT Dec 14 IntentFilter filter = new
		// IntentFilter(de.tubs.ibr.dtn.Intent.NEIGHBOR);
		// filter.addCategory(Intent.CATEGORY_DEFAULT);
		// getContext().registerReceiver(_receiver, filter);
		mStarted = true;

		if (this.takeContentChanged() || mData == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	public List<User> loadInBackground() {
		List<User> list = new ArrayList<User>();
		Cursor cursor = getUsers();
		Log.d("UserListLoader", "Cursor position: " + cursor.getPosition());
		while (cursor.moveToNext()) {
			User user = new User();
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String alias = cursor.getString(cursor.getColumnIndex("alias"));
			user.userName = name;
			user.alias = alias;
			list.add(user);
		}
		return list;
	}

	private Cursor getUsers() {
		Uri uri = Uri.parse("content://in.swifiic.plat.app.suta.andi/users/msngr"); // TODO XXX should not be msngr in helper
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		
		ContentResolver cr = this.getContext().getContentResolver();		
		Cursor c = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		Log.d("UserListLoader getUsers()", "Got users: " + c.getCount());
		return c;
	}

	@SuppressWarnings("unused")
	// will be needed if we have to pull the list from AppHub
	private BroadcastReceiver _receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// if (de.tubs.ibr.dtn.Intent.NEIGHBOR.equals(intent.getAction())) {
			// onContentChanged();
			// }
		}
	};
}
