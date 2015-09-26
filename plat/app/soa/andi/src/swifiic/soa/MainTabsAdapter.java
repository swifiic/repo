package swifiic.soa;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainTabsAdapter extends FragmentPagerAdapter {

	public MainTabsAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Top Rated fragment activity
			return new RechargeFragment();
		case 1:
			// Games fragment activity
			return new AddUserFragment();
		case 2:
			// Movies fragment activity
			return new ManageUserFragment();
		case 3:
			// Movies fragment activity
			return new ManageSWiFiICFragment();

		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count  : equal to number of tabs
		return 4;
	}

}
