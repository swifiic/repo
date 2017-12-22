package in.swifiic.plat.app.suta.andi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nic on 22/12/17.
 */

public class AppListWidgetAdapter extends ArrayAdapter<AppListData> {

    AppListWidgetAdapter(Context context, ArrayList<AppListData> appListDataArrayList) {
        super(context, R.layout.app_list_widget, appListDataArrayList);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.app_list_widget, parent, false);

        AppListData appData = getItem(position);
        final TextView appNameText = (TextView) customView.findViewById(R.id.appName);
        TextView appDescriptionText = (TextView) customView.findViewById(R.id.appDescription);
        Button downloadButton = (Button) customView.findViewById(R.id.downloadButton);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position,0);
            }
        });

        appNameText.setText(appData.appName);
        appDescriptionText.setText(appData.appDescription);
        return customView;
    }
}