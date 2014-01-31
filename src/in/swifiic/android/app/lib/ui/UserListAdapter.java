package in.swifiic.android.app.lib.ui;

import java.util.LinkedList;
import java.util.List;

import in.swifiic.android.app.lib.User;
import in.swifiic.lib.app.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
    private LayoutInflater mInflater = null;
    private List<User> mList = new LinkedList<User>();

    private class ViewHolder {
        public ImageView imageIcon; // TBD XXX integrate user PNG here - for their avatar / image
        public TextView textName;
        public User user;
    }

    public UserListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }
    
    public void swapList(List<User> data) {
    	mList = data;
    	notifyDataSetChanged();
    }

    public int getCount() {
        return mList.size();
    }

    public void add(User n) {
        mList.add(n);
    }

    public void clear() {
        mList.clear();
    }

	public void remove(int position) {
        mList.remove(position);
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.userlist_item, null, true);
            holder = new ViewHolder();
            holder.imageIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.user = mList.get(position);

        holder.textName.setText(holder.user.name);

        return convertView;
    }  
}
