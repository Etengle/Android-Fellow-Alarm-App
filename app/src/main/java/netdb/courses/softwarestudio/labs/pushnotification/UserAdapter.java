package netdb.courses.softwarestudio.labs.pushnotification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Slighten on 2015/1/19.
 */
public class UserAdapter extends BaseAdapter{
    private List<User> mUserList;
    private LayoutInflater mMyInflater;

    public UserAdapter(Context c, ArrayList<User> list) {
        this.mUserList = list;
        mMyInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setList(ArrayList<User> list) {
        this.mUserList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = mMyInflater.inflate(R.layout.user_item, null);
        User user = mUserList.get(position);

        TextView nameTxt = (TextView) convertView.findViewById(R.id.txt_user);
        TextView nicknameTxt = (TextView) convertView.findViewById(R.id.txt_content);

        nameTxt.setText(user.getName());
        nicknameTxt.setText(user.getNickname());

        return convertView;
    }

}
