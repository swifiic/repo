package swifiic.soa;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class AddUserFragment extends Fragment implements OnClickListener,OnFocusChangeListener {

	private EditText etName, etAlias, etEmail, etAddr, etMobileNo, etNotes;
	private ImageButton ibProfilePic, ibIdProof, ibAddrProof, ibCancel, ibUndo, ibDelImage1, ibDelImage2, ibDelImage3;
	private ImageView ivProfile, ivAddrProof, ivIdProof, ivToSet;


	// Regular expression for validating the user data fields like UserName ,EmailAddress , MAC address , Mobile No,Address
	static final String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	//static final String MAC_REGEX = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
	static final String PHONE_REGEX = "^[7-9][0-9]{9}$";
	static final String NAME_REGEX = "^[\\p{L} .'-]{5,15}$";
	static final String ADDRESS_REGEX = "^[\\p{L} \\n \\r.'-]{5,25}$";

	static final String URL = Constants.URL;
	private static PrintStream o = System.out;

	private boolean errorInLogin = false;
	private Integer userId = null;
	// Max size allowed for the profile pics
	private static final long MAX_SIZE = 1 << 18;
	private static final long PROFILE_PIC_SIZE = 1 << 12;


	private MainActivity act;
	ArrayList<BasicNameValuePair> curNameValuePairs = null;
	static SharedPreferences settings = AuthenticationActivity.getSettings();
	private int lastClickedResid = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.adduser, container, false);

		act = (MainActivity) getActivity();
		//Toast.makeText(getActivity(),"in onCreateView",Toast.LENGTH_SHORT).show();
		ImageButton ibCommit = (ImageButton) rootView.findViewById(R.id.imageButtonCommit);
		etName = (EditText) rootView.findViewById(R.id.editTextName);
		etAlias = (EditText) rootView.findViewById(R.id.editTextAlias);
		etEmail = (EditText) rootView.findViewById(R.id.editTextEmailAddress);
		etAddr = (EditText) rootView.findViewById(R.id.editTextAddress);
		etMobileNo = (EditText) rootView.findViewById(R.id.editTextPhone);
		etNotes = (EditText) rootView.findViewById(R.id.editTextNotes);
		ibProfilePic = (ImageButton) rootView.findViewById(R.id.imageButton1);
		ibIdProof = (ImageButton) rootView.findViewById(R.id.imageButton2);
		ibAddrProof = (ImageButton) rootView.findViewById(R.id.imageButton3);
		ibCancel = (ImageButton) rootView.findViewById(R.id.imageButtonCancel);
		ibUndo = (ImageButton) rootView.findViewById(R.id.imageButtonUndo);
		ibDelImage1 = (ImageButton) rootView.findViewById(R.id.ibDeleteImage1);
		ibDelImage2 = (ImageButton) rootView.findViewById(R.id.ibDeleteImage2);
		ibDelImage3 = (ImageButton) rootView.findViewById(R.id.ibDeleteImage3);
		ivProfile = (ImageView) rootView.findViewById(R.id.imageView1);
		ivAddrProof = (ImageView) rootView.findViewById(R.id.imageView2);
		ivIdProof = (ImageView) rootView.findViewById(R.id.imageView3);

		ibCommit.setOnClickListener(this);
		ibUndo.setOnClickListener(this);
		ibProfilePic.setOnClickListener(this);
		ibIdProof.setOnClickListener(this);
		ibAddrProof.setOnClickListener(this);
		ibCancel.setOnClickListener(this);
		ibDelImage1.setOnClickListener(this);
		ibDelImage2.setOnClickListener(this);
		ibDelImage3.setOnClickListener(this);

		etName.setOnClickListener(this);
		etAlias.setOnClickListener(this);
		etAddr.setOnClickListener(this);
		etEmail.setOnClickListener(this);
		etNotes.setOnClickListener(this);
		etName.setOnFocusChangeListener(this);
		etAlias.setOnFocusChangeListener(this);
		etAddr.setOnFocusChangeListener(this);
		etEmail.setOnFocusChangeListener(this);
		etNotes.setOnFocusChangeListener(this);
		Bundle b = getArguments();

       /* the below 'if' block is entered when the intent is coming from an edit user event
        * Fill the user details with the data of the user to be edited
        */
		if (b != null) {
			populate(b);
			etMobileNo.setClickable(false);
			etMobileNo.setFocusable(false);
			//toast("Disabled swapping..");
			act.disableSwapping();
		} else etMobileNo.setOnFocusChangeListener(this);

		// get the common name value pairs from the AuthenticationActivity
		curNameValuePairs = AuthenticationActivity.getCurNamevaluePairs();
		return rootView;
	}

	// displays a toast message
	void toast(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}

	// refreshes the data ( removes the current fragment and replaces it with a new one )
	private void refresh() {
		AddUserFragment f = new AddUserFragment();
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.executePendingTransactions();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.addUserLayout, f);
		transaction.commit();
		//Toast.makeText(getActivity(),"In Refresh..!!",Toast.LENGTH_SHORT).show();
	}

	// used for UNDO option for edit texts . just to keep track of the last edited edit text
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		lastClickedResid = v.getId();
		//Toast.makeText(getActivity(),"focussed .!!",Toast.LENGTH_LONG).show();
	}

	// handles the button click events
	@Override
	public void onClick(View v) {

		Bitmap bm = null;
		switch (v.getId()) {
			// check for correct data and allow editing or adding an user
			case R.id.imageButtonCommit:
				boolean fieldInvalid = false;
				if (!errorInLogin && !(fieldInvalid = fieldsCheck())) {
					new AddEditUserTask(this).execute();
				} else if (fieldInvalid)
					Toast.makeText(getActivity(), "Check the fields", Toast.LENGTH_SHORT).show();
				else Toast.makeText(getActivity(), "Error in Login", Toast.LENGTH_SHORT).show();
				break;
			case R.id.imageButton1: // for profile Pic
				ivToSet = ivProfile;
				takePic();
				break;
			case R.id.imageButton2: // for idProof Pic
				ivToSet = ivIdProof;
				takePic();
				break;
			case R.id.imageButton3: // for addrProof Pic
				ivToSet = ivAddrProof;
				takePic();
				break;
			// refresh the data on Cancel button press
			case R.id.imageButtonCancel:
				refresh();
				break;
			case R.id.imageButtonUndo:
				if (lastClickedResid != -1) {
					switch (lastClickedResid) {
						case R.id.editTextName:
							etName.setText("");
							break;
						case R.id.editTextAlias:
							etAlias.setText("");
							break;
						case R.id.editTextAddress:
							etAddr.setText("");
							break;
						case R.id.editTextEmailAddress:
							etEmail.setText("");
							break;
						case R.id.editTextPhone:
							etMobileNo.setText("");
							break;
						case R.id.editTextNotes:
							etNotes.setText("");
							break;
					}
				}
				//toast("Undo clicked..!! last : "+lastClickedResid);
				break;
			// undo the pics taken for the profile and id,address proofs
			case R.id.ibDeleteImage1:
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.userimg);
				ivProfile.setImageBitmap(bm);
				break;
			case R.id.ibDeleteImage2:
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.icon_id_a);
				ivIdProof.setImageBitmap(bm);
				break;
			case R.id.ibDeleteImage3:
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.passport);
				ivAddrProof.setImageBitmap(bm);
				break;
		}
		//Toast.makeText(getActivity(),"ib"+ibNum+" is clicked...",Toast.LENGTH_LONG).show();
	}

	// takes the pic with an Intent
	private void takePic() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(intent, 0);
	}

	// set the bitmap of the correponding image view with the pic returned from the image capture intent
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap pic = (Bitmap) extras.get("data");
				ivToSet.setImageBitmap(pic);
			}
		}
	}

	// returns the encoded String from a bitmap
	private static String getEncodedStr(Bitmap bmp, boolean smallPic) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (smallPic)
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
		else
			bmp.compress(Bitmap.CompressFormat.JPEG, 10, baos);
		byte[] b = baos.toByteArray();
		String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
		return encodedImage;
	}

	// scales the bitmap to the MAX_SIZE allowed , in case the pic exceeds the allowed size
	static private Bitmap scaleBitmap(Bitmap realImage, long size) {
		float ratio = sizeOf(realImage) > MAX_SIZE ? ((float) sizeOf(realImage)) / MAX_SIZE : 1;
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
				height, true);
		return newBitmap;
	}


	//gets the size of the pic
	static int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}
/* ToastThread to display a toast message
 * called using the runOnUiThread method in the Async tasks where the access to the UI elements is not allowed
 */

	private class ToastThread implements Runnable {
		String msg = null;

		ToastThread(String msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
		}
	}
/*
 * Handles Adding and Editing the user data
 * If the 'userId' is already set , edit user is triggered ,otherwise insert user
 */

	private class AddEditUserTask extends AsyncTask<Void, Void, Void> { // <doInBackground, onProgressUpdate, onPostExecute>

		private final AddUserFragment context;
		private boolean isAdded = false;
		private boolean isEdited = false;


		AddEditUserTask(AddUserFragment ctx) {
			context = ctx;
		}

		@Override
		protected Void doInBackground(Void... params) {
			String url = null;
			//getActivity().runOnUiThread(new ToastThread("In doInBckgrnd of AsyncTask"));
			Log.d("myNewTag", "started-editing");
			HttpResponse response = null;
			try {
				if (settings.contains(URL)) url = settings.getString(URL, null);
				else throw new Exception("URL not set in settings!!");

				String name = etName.getText().toString();
				String alias = etAlias.getText().toString();
				String email = etEmail.getText().toString();
				String addr = etAddr.getText().toString();
				String mobile = etMobileNo.getText().toString();
				String notes = etNotes.getText().toString();
				Bitmap customerPic, idProof, addrProof, miniPic;
				customerPic = scaleBitmap(((BitmapDrawable) ivProfile.getDrawable()).getBitmap(), MAX_SIZE);
				idProof = scaleBitmap(((BitmapDrawable) ivIdProof.getDrawable()).getBitmap(), MAX_SIZE);
				addrProof = scaleBitmap(((BitmapDrawable) ivAddrProof.getDrawable()).getBitmap(), MAX_SIZE);
				miniPic = scaleBitmap(((BitmapDrawable) ivProfile.getDrawable()).getBitmap(), PROFILE_PIC_SIZE);
				String profileStr, customerStr, idProofStr, addrProofStr;
				profileStr = AddUserFragment.getEncodedStr(miniPic, true);
				idProofStr = AddUserFragment.getEncodedStr(idProof, false);
				addrProofStr = AddUserFragment.getEncodedStr(addrProof, false);
				customerStr = AddUserFragment.getEncodedStr(customerPic, false);

				// Add your data
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(curNameValuePairs);

				nameValuePairs.add(new BasicNameValuePair(Constants.usrName_tag, name));
				nameValuePairs.add(new BasicNameValuePair(Constants.alias_tag, alias));
				nameValuePairs.add(new BasicNameValuePair(Constants.usrEmail_tag, email));
				nameValuePairs.add(new BasicNameValuePair(Constants.mobNum_tag, mobile));
				nameValuePairs.add(new BasicNameValuePair(Constants.address_tag, addr));
				nameValuePairs.add(new BasicNameValuePair(Constants.profilePic_tag, profileStr));
				nameValuePairs.add(new BasicNameValuePair(Constants.imageFile_tag, customerStr));
				nameValuePairs.add(new BasicNameValuePair(Constants.idProofFile_tag, idProofStr));
				nameValuePairs.add(new BasicNameValuePair(Constants.addrProofFile_tag, addrProofStr));
				nameValuePairs.add(new BasicNameValuePair(Constants.notes_tag, notes));

				if (userId != null) {
					nameValuePairs.add(new BasicNameValuePair(Constants.userKeyID_tag, Integer.toString(userId)));
					nameValuePairs.add(new BasicNameValuePair(Constants.name_tag, "EditUser"));
				} else {
					nameValuePairs.add(new BasicNameValuePair(Constants.name_tag, "AddUser"));
				}

//				This block is used in many places for doing http POST requests. Maybe it should be moved to another class?
				java.net.URL targetUrl = new URL("http://" + url + "/HubSrvr/Oprtr");
				HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
				conn.setDoOutput(true);
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

				// Execute HTTP Post Request
				int responseCode = conn.getResponseCode();

				boolean isEditTask = userId != null;

				if (responseCode == HttpURLConnection.HTTP_OK) {
					String msg = null;
					if (isEditTask) {
						isEdited = true;
						msg = getResources().getString(R.string.EditSuccess);
					} else {
						isAdded = true;
						msg = getResources().getString(R.string.AdditionSuccess);
					}
					getActivity().runOnUiThread(new ToastThread(msg));
				} else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					MainActivity act = (MainActivity) getActivity();
					getActivity().runOnUiThread(act.new ForceLogoutThread(getResources().getString(R.string.SessionExpired)));
				} else if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
					if (!isEditTask)
						getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.MobileNumberConflict)));
				} else {
					String msg = null;
					if (isEditTask) msg = getResources().getString(R.string.EditUnsuccess);
					else msg = getResources().getString(R.string.AdditionUnsuccess);
					getActivity().runOnUiThread(new ToastThread(msg));
				}
			} catch (HttpHostConnectException e) {
				getActivity().runOnUiThread(new ToastThread(getResources().getString(R.string.HostConnRefused)));
			} catch (Exception e) {
				getActivity().runOnUiThread(new ToastThread("Exception in addEditUser :" + e));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isAdded) context.refresh();
			else if (isEdited) {
				act.enableSwapping();
				android.support.v4.app.FragmentTransaction trans = act.getSupportFragmentManager().beginTransaction();
				trans.remove(AddUserFragment.this).commit();
			}
		}

	} // AsyncTask

	// populates the data of the user being edited , from the bundle passed by the edit user event
	void populate(Bundle b) {

		etName.setText(b.getString(Constants.usrName_tag));
		etAlias.setText(b.getString(Constants.alias_tag));
		etEmail.setText(b.getString(Constants.usrEmail_tag));
		etAddr.setText(b.getString(Constants.address_tag));
		etMobileNo.setText(b.getString(Constants.mobNum_tag));
		etNotes.setText(b.getString(Constants.notes_tag));
		Resources rsc = getActivity().getResources();
		Bitmap profile = null, idProof = null, addrProof = null;
		String profileStr = b.getString(Constants.imageFile_tag);
		String idProofStr = b.getString(Constants.idProofFile_tag);
		String addrProofStr = b.getString(Constants.addrProofFile_tag);
		userId = Integer.decode(b.getString(Constants.userKeyID_tag));

		if (profileStr != null && !profileStr.equals("")) profile = getBitmap(profileStr);
		else profile = BitmapFactory.decodeResource(rsc, R.drawable.userimg);

		if (idProofStr != null && !idProofStr.equals("")) idProof = getBitmap(idProofStr);
		else idProof = BitmapFactory.decodeResource(rsc, R.drawable.icon_id_a);

		if (addrProofStr != null && !addrProofStr.equals("")) addrProof = getBitmap(addrProofStr);
		else addrProof = BitmapFactory.decodeResource(rsc, R.drawable.passport);

		ivProfile.setImageBitmap(profile);
		ivIdProof.setImageBitmap(idProof);
		ivAddrProof.setImageBitmap(addrProof);

	}

	// decodes the byte string and returns the BitMap
	private static Bitmap getBitmap(String imageStr) {

		if (imageStr == null || imageStr.equals("")) return null;
		byte image[] = Base64.decode(imageStr, Base64.DEFAULT);
		Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
		return bmp;
	}

	// Check for the fields using the Reg Ex .Allow insertion or edition of an user , only if the fields are proper
	private boolean fieldsCheck() {
		boolean isError = false;
		if (!etName.getText().toString().trim().matches(NAME_REGEX)) {
			etName.setError("5-15 chars");
			isError = true;
		}
		if (!etEmail.getText().toString().trim().matches(EMAIL_REGEX)) {
			etEmail.setError("Invalid Email");
			isError = true;
		}
		if (!etAddr.getText().toString().trim().matches(ADDRESS_REGEX)) {
			etAddr.setError("5-25 chars");
			isError = true;
		}
		if (!etMobileNo.getText().toString().trim().matches(PHONE_REGEX)) {
			etMobileNo.setError("Invalid Mobile No.");
			isError = true;
		}
		return isError;
	}
}
