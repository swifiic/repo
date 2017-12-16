package in.swifiic.plat.helper.andi.ui;

import java.util.List;

import in.swifiic.plat.helper.andi.GenericService;
import in.swifiic.plat.helper.andi.User;
import in.swifiic.plat.helper.andi.UserListLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

public class UserChooserFragment extends ListFragment implements
    LoaderManager.LoaderCallbacks<List<User>> {
    
    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID = 1;
    
    private UserListAdapter mAdapter = null;
    private GenericService mService = null;
    private Boolean mBound = false;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        
    	public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((GenericService.LocalBinder)service).getService();
            // initialize the loaders
            getLoaderManager().initLoader(LOADER_ID,  null, UserChooserFragment.this);
        }

        public void onServiceDisconnected(ComponentName name) {
            getLoaderManager().destroyLoader(LOADER_ID);
            mService = null;
        }
    };
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        UserListAdapter nla = (UserListAdapter) this.getListAdapter();
        User n = (User) nla.getItem(position);
        
        Intent data = new Intent();
        data.putExtra("userName", n.userName);
        data.putExtra("alias", n.alias);

        // select the item
        getActivity().setResult(1, data);
        getActivity().finish();
        
        // call super-method
        super.onListItemClick(l, v, position, id);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBound = false;
    }

    @Override
    public void onDestroy() {
        if (mBound) {
            getLoaderManager().destroyLoader(LOADER_ID);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (!mBound) {
            getActivity().bindService(new Intent(getActivity(), GenericService.class), mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("No Users");

        // create a new list adapter
        mAdapter = new UserListAdapter(getActivity());

        // set listview adapter
        setListAdapter(mAdapter);
        
        // Start out with a progress indicator.
        setListShown(false);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new UserListLoader(getActivity(), mService);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> users) {
        synchronized (mAdapter) {
            mAdapter.swapList(users);
        }
        
        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {
    }
}
