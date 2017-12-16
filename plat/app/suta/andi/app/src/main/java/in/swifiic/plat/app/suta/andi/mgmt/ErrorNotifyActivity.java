package in.swifiic.plat.app.suta.andi.mgmt;

import in.swifiic.plat.app.suta.andi.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ErrorNotifyActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  
    super.onCreate(savedInstanceState);
    setContentView(R.layout.notificationact);
    String msg = getIntent().getExtras().getString("text");
    if(null != msg){
    	TextView text = (TextView) findViewById(R.id.textViewNotification);
    	text.setText(msg);
    }
  }

} 
