package swifiic.soa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 
public class ManageUserFragment extends Fragment implements OnItemClickListener,OnItemLongClickListener {
 
    //private static final String ipAddr = AuthenticationActivity.getIpAddress();
	public static final String URL = Constants.URL;
	// get the shared preferences
	public static SharedPreferences settings = AuthenticationActivity.getSettings();
	private static final int dpWidth = 75; // MAX allowed height of the profile pic
    private static final int dpHeight = 75; // MAX allowed width of the profile pic 
   
    
    private UserListAdapter adapter ;
    // keeps track of the user data (listview is populated using this list)
    private ArrayList<HashMap<String,String>> users = new ArrayList<HashMap<String,String>>();
    private ArrayList<BasicNameValuePair> commonNameValuePairs ;
    
    // transaction and recharge user dialogs
    private  Dialog transactionDialog = null;
    private  Dialog rechargeDialog = null;
    static ManageUserFragment curInstance = null;
    static String msg = "";
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	curInstance = this;
        View rootView = inflater.inflate(R.layout.user, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.userList);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        adapter = new UserListAdapter(getActivity(),R.layout.list_item);
        listView.setAdapter(adapter);
        commonNameValuePairs = AuthenticationActivity.getCurNamevaluePairs();
        refresh();
        return rootView;
    }
    
   // refetches the user list from the server and populates the list view 
    static void refresh(){
    	if (curInstance!=null)  {
    		curInstance.new GetUsersOrTransactionsTask().execute("GetUsers");
    	}
    }
    
    // Gets the user List or transaction list for a specific user
    private class GetUsersOrTransactionsTask extends AsyncTask<String,Void,byte[]>{
    	// says whether the current task is for getting the user list or the transaction data for a specific user
		boolean isTransaction = false;
		HttpResponse response = null;
		byte[] listBytes = null;
		int responseCode;
		@Override
		protected byte[] doInBackground(String... params) {
		//	getActivity().runOnUiThread(new ToastThread("In getUsers"));
		    String url = null;
			try {
			if (settings.contains(URL)) url = settings.getString(URL,null);
			else throw new Exception("URL not set in settings!!");
			String req = params[0];
			
			String userId = null;
			// GetTransactions is the request name for getting the transactions for a specific user 
			if (req.equals("GetTransactions")){
		       isTransaction = true;
			   userId = params[1];  // userId is set only incase of transactions
			}
		    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.addAll(commonNameValuePairs);
	        // Adding the user Id for transactions and the request Tag 
		      if (isTransaction){
		    		nameValuePairs.add(new BasicNameValuePair(Constants.userKeyID_tag,userId));
		    		nameValuePairs.add(new BasicNameValuePair(Constants.name_tag,"GetTransactions"));
			   }
		      else  nameValuePairs.add(new BasicNameValuePair(Constants.name_tag,"ListUser"));

				java.net.URL targetUrl = new URL("http://" + url + "/HubSrvr/Oprtr");
				HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");

				Uri.Builder builder = new Uri.Builder();

				for (NameValuePair bnvp: nameValuePairs) {
					builder.appendQueryParameter(bnvp.getName(), bnvp.getValue());
				}

				String query = builder.build().getEncodedQuery();
				Log.d("SOA", query);
				try {
					BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
					bos.write(query.getBytes("utf-8"));
					bos.flush();
					bos.close();
				} catch (IOException e) {

				}
				Log.d("SOA", "DEBUGCODE" + conn.getResponseCode());
				responseCode = conn.getResponseCode();
				if (200 <= responseCode && responseCode <= 299) {
					listBytes = IOUtils.toByteArray(conn.getInputStream());
				}

				Log.d("SOA", "DEBUGBYTES" + listBytes.length);

			} 
			catch(HttpHostConnectException e){
				 getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.HostConnRefused))); 
			}
			catch(Exception e){
		    	getActivity().runOnUiThread(new ToastThread("Exception in getting user list : "+e));
			 }
		    return listBytes;
		    }
		@Override
		protected void onPostExecute(byte[] result) {
			super.onPostExecute(result);
		    if (result==null) return ;
			if (responseCode==417) { // 417 for EXPECTATION FAILED
							String errorMsg = null;
					         if (isTransaction) errorMsg = getResources().getString(R.string.CannotFetchTransactions);
					         else errorMsg = getResources().getString(R.string.CannotFetchUsers);
							 toast(errorMsg);
						}
			if (responseCode==HttpURLConnection.HTTP_UNAUTHORIZED) { // 417 for EXPECTATION FAILED
						    ((MainActivity)getActivity()).forceLogout(getResources().getString(R.string.SessionExpired));
								}
							  
			if (responseCode==HttpURLConnection.HTTP_OK){
				if (listBytes!=null){
									//for(int i=0;i<listBytes.length;i++)
									//	System.out.println(listBytes[i]);
									// convert the byte array into an ArrayList which contains the user data or transaction data
									ArrayList<HashMap<String,String>> list = 
											(ArrayList<HashMap<String, String>>) SerializationUtils.deserialize(listBytes);
									ArrayList<String> transactions = new ArrayList<String>();
									// reload the userList if this is not a transaction List
					if (!isTransaction){
										users.clear();
										users.addAll(list);
										adapter.refreshAdapter(list); // refreshes the adapter for the userlist listview
										toast(getResources().getString(R.string.RefreshedUsers));
					}
					else {

						if (list.size()>0) {
											transactionDialog.setContentView(R.layout.transaction_list);
										 	transactionDialog.setTitle(getResources().getString(R.string.Transactions));
										 	TransactionAdapter adapter = new TransactionAdapter(getActivity()
										 			,R.layout.transaction_item,list);
										 	ListView transList = (ListView) transactionDialog.findViewById(R.id.transactionList);
										 	transList.setAdapter(adapter);
										 	transactionDialog.show();
						}
						else  toast(getResources().getString(R.string.NoTransactions));
					}
				}
				else  toast(getResources().getString(R.string.NoDataReceived));
			}
					
		}
		
	}
	
/// decodes the byte string and converts to a bitmap 
 private static Bitmap getBitmap(String imageStr){
	if (imageStr==null || imageStr.equals("")) return null;
	byte image[] = Base64.decode(imageStr,Base64.DEFAULT);
    Bitmap bmp= BitmapFactory.decodeByteArray(image,0,image.length);
    return bmp;
}
	
 
private class TransactionAdapter extends ArrayAdapter<HashMap<String,String>>{
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
	Context context ; 
	TransactionAdapter(Context context, int resource,ArrayList<HashMap<String,String>> list){
		super(getActivity(), resource);
		this.context = getActivity();
		this.list = list;
	}
	
   @Override
   public View getView(int position, View view, ViewGroup parent) {
      
	   LayoutInflater inflater = LayoutInflater.from(getActivity());
	   View row = inflater.inflate(R.layout.transaction_item, parent, false);
	   TextView tvType,tvAmt,tvDate;
	   tvType = (TextView) row.findViewById(R.id.tvTransactionType);
	   tvAmt = (TextView)row.findViewById(R.id.tvTransactionAmount);
	   tvDate = (TextView) row.findViewById(R.id.tvTransactionDate);
	   HashMap<String,String> map = list.get(position);
	   String timeStamp = map.get("Time");
	   DateTime then = new DateTime(getDateTime(timeStamp), DateTimeZone.forID("Asia/Kolkata"));
	   DateTime now = new DateTime(new Timestamp(new java.util.Date().getTime()), DateTimeZone.forID("Asia/Kolkata"));
	   int days = Days.daysBetween(then.withTimeAtStartOfDay() , now.withTimeAtStartOfDay() ).getDays();
	   tvType.setText(map.get("Details"));
	   tvAmt.setText(map.get("Amount"));
	   tvDate.setText(String.format("%-3d",days)+" days ago");
	   return row;
   }
   @Override
	public int getCount() {
	   return list.size();
}

}

// ArrayAdapter for the UserList list view	
private class UserListAdapter extends ArrayAdapter<HashMap<String,String>>{
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
	Context context ; 
	UserListAdapter(Context context, int resource) {
		super(getActivity(), resource);
		this.context = getActivity();
	}
	// sets the user data list
   void setList(ArrayList<HashMap<String,String>> list){
	   this.list = list;
   }
   @Override
   public View getView(int position, View view, ViewGroup parent) {
       LayoutInflater inflater = LayoutInflater.from(getActivity());
	   View row = inflater.inflate(R.layout.list_item, parent, false);
      // Adding GestureDetector for double tap
	  
	   GestureDoubleTap gestureDoubleTap = new GestureDoubleTap(position);
	   final GestureDetector gestureDetector = new GestureDetector(getActivity(),gestureDoubleTap);

	   row.setOnTouchListener(new View.OnTouchListener() {
	       @Override
	       public boolean onTouch(View view, MotionEvent motionEvent) {
	           return gestureDetector.onTouchEvent(motionEvent);
	       }

	   });
	   
	   
	   
      ImageView ivDp = (ImageView) row.findViewById(R.id.ivDpInList); 
      TextView tvName,tvMobile;
      tvName = (TextView) row.findViewById(R.id.tvUserNameInList);
      tvMobile = (TextView) row.findViewById(R.id.tvMobileNumberInList);
      
      HashMap<String,String> map = list.get(position); 
      String imgStr = map.get(Constants.imageFile_tag);
      if (imgStr!=null && !imgStr.equals("")){
    	  //Bitmap pic = getBitmap(imgStr);
    	  Bitmap orig = getBitmap(imgStr);
    	  Bitmap pic = scaleDown(getBitmap(imgStr));
    	  ivDp.setImageBitmap(pic);
    	}
      //else ivDp.setImageBitmap(bm)
      ImageButton ibDelete = (ImageButton) row.findViewById(R.id.ibDeleteUser);
      ibDelete.setOnClickListener(new MyOnClickListener(position));
      String userName = map.get(Constants.usrName_tag);
      String mobileNum = map.get(Constants.mobNum_tag);
      tvName.setText(userName);
      tvMobile.setText(mobileNum);
      
      return row;
   }
  //overriden method to get the number of items in the list
   @Override
public int getCount() {
	   //Toast.makeText(getActivity(),"In getCount : "+list.size(),Toast.LENGTH_LONG).show();
	   return list.size();
}
   // refeshes the adapter for the list view
   synchronized void refreshAdapter(ArrayList<HashMap<String,String>> list) {   
	    this.list.clear();
	    this.list.addAll(list);
	    notifyDataSetChanged();
	   }
}
// returns the size of the bitmap
static int sizeOf(Bitmap data) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
        return data.getRowBytes() * data.getHeight();
    } else {
        return data.getByteCount();
    }
}
// scales down the bitmap to the allowed dimensions
static Bitmap scaleDown(Bitmap bmp){
	Bitmap newbitMap = Bitmap.createScaledBitmap(bmp,dpWidth,dpHeight,true);
	return newbitMap;
}

/*
 * Single tap : displays the list of user transactions
 * Double tap : prompts for deletion of the user
 * Long tap : editing the user data
 * click on the dollar (recharge) button for recharge : allows credit  to the user's account
 */

// OnClickListener to handle the recharge button click, allows credit
private class MyOnClickListener implements OnClickListener {
   
	int position=-1;// clicked row no.
   MyOnClickListener(int position) {
	   this.position = position;
	}
	
	@Override
	public void onClick(View v) {
		// Recharge dialog that enables credit option
		// setting up the recharge dilalog for credit
		rechargeDialog = new Dialog(getActivity(),android.R.style.Theme_Black);
		rechargeDialog.setContentView(R.layout.recharge_user);
		rechargeDialog.setTitle(getResources().getString(R.string.RechargeUser));
		
		final EditText etRecharge = (EditText) rechargeDialog.findViewById(R.id.etRecharge);
		TextView tvUserId = (TextView) rechargeDialog.findViewById(R.id.tvUserIdInRecharge);
		TextView tvUserName = (TextView) rechargeDialog.findViewById(R.id.tvUserNameInRecharge);
		TextView tvRemCredits = (TextView) rechargeDialog.findViewById(R.id.tvRemCredits);
		TextView tvLastAudit = (TextView) rechargeDialog.findViewById(R.id.tvLastAudit);
		
		Button bCredit = (Button)rechargeDialog.findViewById(R.id.bCredit);
		
		
		//tv.setText((CharSequence) text);
        //new GetUsersOrTransactionsTask(tv).execute(ipAddr,"GetTransactions",userId);
	    String notes= "";
	    String name = users.get(position).get("usrName");
	    String remCredits = users.get(position).get("remainingCreditPostAudit");
	    String lastAudit = users.get(position).get("lastAuditedActivityAt");
	    final String userId  = users.get(position).get("userKeyID");
	    
	    tvUserId.setText((CharSequence)userId);
	    tvUserName.setText((CharSequence)name);
	    tvRemCredits.setText((CharSequence)remCredits);
	    tvLastAudit.setText((CharSequence)lastAudit);
	    
	    // handles the credit event
	    bCredit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!etRecharge.getText().toString().equals("")){
					new RechargeUserTask().execute(userId,etRecharge.getText().toString(),"Credit");
					etRecharge.setText((CharSequence)"");
				}
				else toast("Please enter amount");
			}
		});

	    
	    // prompt the user in case of an incomplete action
rechargeDialog.setOnKeyListener(new Dialog.OnKeyListener() {

        @Override
        public boolean onKey(final DialogInterface arg0, int keyCode,
                KeyEvent event) {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!etRecharge.getText().toString().equals("")){
                	AlertDialog.Builder builder = new AlertDialog.Builder(ManageUserFragment.this.getActivity());
        			Resources res = getResources();
                	builder.setMessage(res.getString(R.string.AreUSure));
        			builder.setPositiveButton(res.getString(R.string.Yes), new DialogInterface.OnClickListener() {
        		        public void onClick(DialogInterface dialog, int buttonId) {
        		            dialog.dismiss();
            		        arg0.cancel();
        		        }
        		    });
        			builder.setNegativeButton(res.getString(R.string.No), null);
        			AlertDialog aDialog = builder.create();    
        		 	aDialog.show();
                }
                else rechargeDialog.dismiss();
                return true;
        	}
            return false;
        }
});
			rechargeDialog.show();
		
	}
}
	
// Delete User task to disable  user
private class DeleteUserTask extends AsyncTask<String,Void,Void>{
	@Override
	protected Void doInBackground(String... params) {
		String url = null;
		HttpResponse response = null;
		try {	
			if (settings.contains(URL)) {
				url = settings.getString(URL,null);
			} else {
				throw new Exception("URL not set in settings!!");
			}
			String userId = params[0];

			ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
			Log.d("SOA", "DoingDELETETASK");
			nameValuePairs.addAll(commonNameValuePairs);
			nameValuePairs.add(new BasicNameValuePair(Constants.name_tag,"DeleteUser"));
			nameValuePairs.add(new BasicNameValuePair(Constants.userKeyID_tag,userId));

//			HttpClient httpclient = new DefaultHttpClient();
//			HttpPost httppost = new HttpPost("http://"+url+"/HubSrvr/Oprtr");
//
//			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//			response = httpclient.execute(httppost);
//			int responseCode = response.getStatusLine().getStatusCode();

			java.net.URL targetUrl = new URL("http://" + url + "/HubSrvr/Oprtr");
			HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");

			Uri.Builder builder = new Uri.Builder();

			for (NameValuePair bnvp: nameValuePairs) {
				builder.appendQueryParameter(bnvp.getName(), bnvp.getValue());
			}

			String query = builder.build().getEncodedQuery();
			Log.d("SOA", "myQUERY:"+query);
			try {
				BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
				bos.write(query.getBytes("utf-8"));
				bos.flush();
				bos.close();
			} catch (IOException e) {

			}

			int responseCode = conn.getResponseCode();


			Log.d("SOA", "Response Code => " + responseCode);
			if (responseCode==HttpURLConnection.HTTP_OK) {
				getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.DisableSuccess)));
			} else if (responseCode==HttpURLConnection.HTTP_UNAUTHORIZED) {
				MainActivity act = (MainActivity)getActivity();
				getActivity().runOnUiThread(act.new ForceLogoutThread(getResources().getString(R.string.SessionExpired)));
			} else  {
				getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.DisableUnsuccess)));
			}
	    } catch(HttpHostConnectException e){
			getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.HostConnRefused)));
		} catch(Exception e){
    		getActivity().runOnUiThread(new ToastThread("Exception in disabling user : "+e));
    	}
		return null;
	}
}
	
// Credit is carried out by this task
private class RechargeUserTask extends AsyncTask<String,Void,Void>{
	String event = null;
	
	@Override
	protected Void doInBackground(String... params) {
		String url = null;
//		HttpResponse response = null;
	try {
	    if (settings.contains(URL)) {
			url = settings.getString(URL,null);
		} else {
			throw new Exception("URL not set in settings!!");
		}

		String userId = params[0];
		String recharge = params[1];
		event = params[2];

		ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
	    
	    nameValuePairs.addAll(commonNameValuePairs);
	    nameValuePairs.add(new BasicNameValuePair(Constants.name_tag,"RechargeUser"));

		nameValuePairs.add(new BasicNameValuePair(Constants.CreditUserId_tag,userId));
	    
	    nameValuePairs.add(new BasicNameValuePair(Constants.Amount_tag,recharge));
	    nameValuePairs.add(new BasicNameValuePair(Constants.EventNotes_tag,event+" from SOA app"));
	    nameValuePairs.add(new BasicNameValuePair(Constants.Details_tag,event));

		java.net.URL targetUrl = new URL("http://" + url + "/HubSrvr/Oprtr");
		HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");

		Uri.Builder builder = new Uri.Builder();

		for (BasicNameValuePair bnvp: nameValuePairs) {
			builder.appendQueryParameter(bnvp.getName(), bnvp.getValue());
		}

		String query = builder.build().getEncodedQuery();
		try {
			BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
			bos.write(query.getBytes("utf-8"));
			bos.flush();
			bos.close();
		} catch (IOException e) {

		}
        
		int responseCode = conn.getResponseCode();
		Log.v("ERROR", "Response Code => " + responseCode);
		String s = null;

		if (responseCode==HttpURLConnection.HTTP_OK) {
			String msg = null;

			msg = getResources().getString(R.string.CreditSuccess);
			getActivity().runOnUiThread(new ToastThread(msg+" for "+userId));
			//    Toast.makeText(context.getActivity(),"Response Body => " + responseBody,Toast.LENGTH_LONG).show();
		} else if (responseCode==HttpURLConnection.HTTP_UNAUTHORIZED){
		MainActivity act = (MainActivity)getActivity();
		getActivity().runOnUiThread(act.new ForceLogoutThread(getResources().getString(R.string.SessionExpired)));
		} else {
			msg = getResources().getString(R.string.CreditUnsuccess);
			getActivity().runOnUiThread(new ToastThread(msg));
		}
    } catch(Exception e) {
		getActivity().runOnUiThread(new ToastThread("Exceptionin RechargeUser Task: "+e));
		Log.e("SOA", "Exeception!", e);
	}
	return null;
	}
}
	// toast thread to display the toast messages     
	private class ToastThread implements Runnable{
		String msg = null;
		ToastThread(String msg){
			this.msg = msg;
		}
		@Override
		public void run() {
			Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
		}
	}
// handles the long tap to edit the user data
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		//Toast.makeText(getActivity(),"ItemClicked at "+position+" ; "+parent.getId()+", "+R.id.userList,Toast.LENGTH_SHORT).show();
		
		try {
		switch(parent.getId()){
		  case R.id.userList:
			//  Toast.makeText(getActivity(),"Clicked "+position,Toast.LENGTH_SHORT).show();
			  HashMap<String,String> map = users.get(position);
			  AddUserFragment fragment = new AddUserFragment();
			  Bundle b = new Bundle();
			  Iterator<String> it = map.keySet().iterator();
			  while (it.hasNext()){
				String key = it.next();
				b.putString(key,map.get(key));
			  }
			  fragment =(AddUserFragment)AddUserFragment.instantiate(getActivity(), AddUserFragment.class.getName(),b);
			//  fragment.populate(map.get(Name),map.get(Alias),map.get(EmailAddress), map.get(Address), 
				//	  map.get(MobileNum),map.get(AddrVerifNotes), profile, idProof, addrProof);
			  
			  fragment.setArguments(b);
			  FragmentManager fm = getActivity().getSupportFragmentManager();
			  fm.executePendingTransactions();
			  FragmentTransaction transaction = fm.beginTransaction();
			  transaction.replace(R.id.userListLayout,fragment,"EDIT_FRAGMENT").addToBackStack( "tag" );
			  transaction.commit();
			  break;
			  //getActivity().getSupportFragmentManager().beginTransaction().hide(this).commit();
			  //getActivity().getSupportFragmentManager().beginTransaction().show(fragment).commit();
		}
		}
		catch(Exception e){
//			String stTrace = ExceptionUtils.getFullStackTrace(e);
//			Toast.makeText(getActivity(),"Exception in onItemClick :\n"+stTrace,Toast.LENGTH_LONG).show();
//			 Log.e("ERROR",stTrace);
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		
	}

	// listener to handle single ,long and double taps
	class GestureDoubleTap extends GestureDetector.SimpleOnGestureListener {

	    String text =  "No Transactions";
	    int position;
	    String userId ;
	    GestureDoubleTap(int position) {
	    	this.position = position;
	    	 userId = users.get(position).get(Constants.userKeyID_tag);
	    }
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		// handles the doubletap event , prompts for deletion 
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			//toast(getActivity(),"In onSingleTapConfirmed");
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Confirm Delete..!!");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int buttonId) {
		        	new DeleteUserTask().execute(userId);
		        }
		    });
			builder.setNegativeButton("No", null);
			AlertDialog dialog = builder.create();    
			dialog.show();
			return true;
		}
		// handles the long tap event, to edit the user data 
		@Override
		public void onLongPress(MotionEvent me) {
			//toast(getActivity(),"Long Tap");
		try {
			HashMap<String,String> map = users.get(position);
			  AddUserFragment fragment = new AddUserFragment();
			  Bundle b = new Bundle();
			  Iterator<String> it = map.keySet().iterator();
			  while (it.hasNext()){
				String key = it.next();
				b.putString(key,map.get(key));
			  }
			  fragment =(AddUserFragment)AddUserFragment.instantiate(getActivity(), AddUserFragment.class.getName(),b);
			//  fragment.populate(map.get(Name),map.get(Alias),map.get(EmailAddress), map.get(Address), 
				//	  map.get(MobileNum),map.get(AddrVerifNotes), profile, idProof, addrProof);
			  
			  fragment.setArguments(b);
			  FragmentManager fm = getActivity().getSupportFragmentManager();
			  fm.executePendingTransactions();
			  FragmentTransaction transaction = fm.beginTransaction();
			  transaction.replace(R.id.userListLayout,fragment,"EDIT_FRAGMENT").addToBackStack( "tag" );
			  transaction.commit();
		}
		
		catch(Exception e){
//			String stTrace = ExceptionUtils.getFullStackTrace(e);
//			Toast.makeText(getActivity(),"Exception in onItemClick :\n"+stTrace,Toast.LENGTH_LONG).show();
//			 Log.e("ERROR",stTrace);
		}
		}
		
	    //single tap handler , gets the transactions of an user
		@Override
	    public boolean onSingleTapConfirmed(MotionEvent e) {
	        //toast(getActivity(),"In double Tap");
	    	transactionDialog = new Dialog(getActivity(),android.R.style.Theme_Black);
			//tv.setText((CharSequence) text);
	        new GetUsersOrTransactionsTask().execute("GetTransactions",userId);
			return true;
	    }

	}
	
private static DateTime getDateTime(String timeStamp){
	    try {
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		    java.util.Date  parsedDate = dateFormat.parse(timeStamp);
		    return new DateTime(parsedDate);
	    } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
}
	
// displays a toast message	
 void toast(String msg){
	Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
}
	
}