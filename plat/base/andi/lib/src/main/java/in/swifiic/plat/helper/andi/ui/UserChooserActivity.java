package in.swifiic.plat.helper.andi.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class UserChooserActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            // During initial setup, plug in the messages fragment.
            UserChooserFragment chooser = new UserChooserFragment();
            chooser.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, chooser).commit();
        }
    }
    
}
