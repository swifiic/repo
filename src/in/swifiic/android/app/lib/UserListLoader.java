package in.swifiic.android.app.lib;

import java.util.ArrayList;
import java.util.List;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
//import de.tubs.ibr.dtn.api.Node;

public class UserListLoader extends AsyncTaskLoader<List<User>> {
	
	@SuppressWarnings("unused") // will be needed if we have to pull the list from AppHub
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
		
        //AT Dec 14 IntentFilter filter = new IntentFilter(de.tubs.ibr.dtn.Intent.NEIGHBOR);
        //   filter.addCategory(Intent.CATEGORY_DEFAULT);
        //   getContext().registerReceiver(_receiver, filter);
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
		// For now use a hard coded list - later fetch it from Preferences or using DTN
		User usr1 = new User();
		User usr2 = new User();
		usr1.id = "1";
		usr2.id= "2";
		usr1.name = "Some User One";
		usr2.name = "Some user Two";
		usr1.imageArray = usr2.imageArray = new byte[0];
		List<User> list = new ArrayList<User>();
		list.add(usr1); list.add(usr2);
		return list;
	}

	@SuppressWarnings("unused") // will be needed if we have to pull the list from AppHub
	private BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if (de.tubs.ibr.dtn.Intent.NEIGHBOR.equals(intent.getAction())) {
            //	onContentChanged();
            //}
        }
    };
}
