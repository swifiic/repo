package swifiic.soa;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	public static final String URL = Constants.URL;
	private MyViewPager viewPager;
	//private ViewPager viewPager;
	private MainTabsAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	//private Resources res =
	private String[] tabs =  null;

	// get the shared preferences
	public static final SharedPreferences settings = AuthenticationActivity.getSettings();
	public static final String IP_ADDRESS = "IP_ADDRESS";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// handling the cookies
		CookieHandler.setDefault(new CookieManager());
		viewPager = (MyViewPager) findViewById(R.id.pager);
		//viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new MainTabsAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabs =  new String[]{getResources().getString(R.string.Recharge),
				getResources().getString(R.string.addUser),
				getResources().getString(R.string.Users),
				getResources().getString(R.string.swifiic)};

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
		}
		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	// enables the swapping after disabling
	public void enableSwapping(){
		viewPager.setBlockSwipe(false);
	}
	// disables the swap , when the user data is being edited
	public void disableSwapping(){
		viewPager.setBlockSwipe(true);
	}

	/* Handles the backpress events ,
        * asking for confirmation of the lost changes , deletion of an user , incompletely filled data
     */
	@Override
	public void onBackPressed() {
		final AddUserFragment f = (AddUserFragment)getSupportFragmentManager().findFragmentByTag("EDIT_FRAGMENT");
		if (f!=null && f.isVisible()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			Resources res = getResources();
			builder.setMessage(res.getString(R.string.ChangesWillBeLost));
			builder.setPositiveButton(res.getString(R.string.Ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int buttonId) {
					MainActivity.super.onBackPressed();
					android.support.v4.app.FragmentTransaction trans = MainActivity.this.getSupportFragmentManager().beginTransaction();
					trans.remove(f).commit();
					enableSwapping();
				}
			});
			builder.setNegativeButton(res.getString(R.string.Cancel), null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else super.onBackPressed();
	}

	// inflates the menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// handles the menu item selection like changing the settings , logout and refreshing the user list
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//new LogoutTask().execute(ipAddr);
		switch(item.getItemId()){
			case R.id.log_out:
				logOut();
				break;
			case R.id.refresh_users:
				ManageUserFragment.refresh(); break;
			case R.id.action_settings:
				final Dialog dialog = new Dialog(MainActivity.this,android.R.style.Theme_Black);
				dialog.setContentView(R.layout.change_settings);
				dialog.setTitle(getResources().getString(R.string.action_settings));
				final EditText etIpAddr = (EditText) dialog.findViewById(R.id.etIpAddress);
				final Button bSave = (Button) dialog.findViewById(R.id.bSaveChanges);
				if (settings.contains(URL)) etIpAddr.setText(settings.getString(URL, null));

				// change the shared preferences for URL as the savechanges button is clicked
				bSave.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String ipAddr =etIpAddr.getText().toString().trim();
						if (ipAddr==null || ipAddr.equals("")) toast("Please fill..!!");
						else {
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(URL,ipAddr);
							editor.commit();
							toast("Changes saved!!");

							dialog.dismiss();
							forceLogout(getResources().getString(R.string.URLChanged));
						}
					}
				});
				dialog.show();
		}
		return true;
	}

	public void logOut(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
		builder.setMessage(res.getString(R.string.logging_out));
		builder.setPositiveButton(res.getString(R.string.Ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int buttonId) {
				dialog.dismiss();
				new LogOutTask().execute();
			}
		});
		builder.setNegativeButton(res.getString(R.string.Cancel), null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public class ForceLogoutThread implements Runnable {
		String msg = null;
		ForceLogoutThread(String msg){
			this.msg = msg;
		}
		@Override
		public void run() {
			forceLogout(msg);
		}
	}

	public void forceLogout(String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.Ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int buttonId) {
				dialog.dismiss();
				new LogOutTask().execute();
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	//Logs out the user , removes his/her data from the shared preferences
	public class LogOutTask extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void[] params) {
			Log.d("newTag", "LoggingOutNow");
			try {
				String authUrl = AuthenticationActivity.getUrl();
				ArrayList<BasicNameValuePair> nmPairs =
						new ArrayList<BasicNameValuePair>(AuthenticationActivity.getCurNamevaluePairs());
				//runOnUiThread(new ToastThread(nmPairs.con
				nmPairs.add(new BasicNameValuePair("name","Logout"));

				java.net.URL url = new URL("http://" + authUrl + "/HubSrvr/Oprtr");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");

				Uri.Builder builder = new Uri.Builder();

				for (BasicNameValuePair bnvp: nmPairs) {
					builder.appendQueryParameter(bnvp.getName(), bnvp.getValue());
				}

				String query = builder.build().getEncodedQuery();
				Log.d("newTag", query);

				try {
					BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
					bos.write(query.getBytes("utf-8"));
					bos.flush();
					bos.close();
				} catch (IOException e) {

				}

			}
			catch(MalformedURLException e) {
				runOnUiThread(new ToastThread(getResources().getString(R.string.HostConnRefused)));
			}
			catch(Exception e){
				e.printStackTrace();
			}

			try {
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(Constants.JSESSIONID);
				editor.remove(Constants.USERNAME);
				editor.commit();
				runOnUiThread(new ToastThread(getResources().getString(R.string.logging_out)));
				startActivity(new Intent(MainActivity.this,AuthenticationActivity.class));
				finish();

			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}


	private class ToastThread implements Runnable {
		String msg = null;
		ToastThread(String msg){
			this.msg  = msg;
		}
		@Override
		public void run(){
			toast(msg);
		}
	}


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	// displays a toast message
	public void toast(String s){
		Toast.makeText(MainActivity.this,s, Toast.LENGTH_SHORT).show();
	}

}
